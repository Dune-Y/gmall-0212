package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {


    @Autowired
    private SpuDescMapper descMapper;

    @Autowired
    private SpuAttrValueService baseService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuByCidAndPage(PageParamVo paramVo, Long categoryId) {


        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //判断分类Id是否为0,如果不为0添加分类的过滤条件 (习惯成自然)
        if(categoryId != 0) {
            wrapper.eq("category_id",categoryId);
        }
        String key = paramVo.getKey();
        //获取查询关键字 ,判断是否为空,如果不为空添加条件查询 (习惯成自然)
        if(StringUtils.isNotBlank(key)){
            //这个方法需要重点了解
            wrapper.and(t-> t.eq("id",key).or().like("name",key));
        }
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                wrapper
        );
        return new PageResultVo(page);
    }

    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuVo) {
        // 1.保存spu相关
        // 1.1 保存spu基本信息 spu_info
        Long spuId = this.saveSpu(spuVo);
        // 1.2. 保存spu的描述信息 spu_info_desc
        this.saveSpuDesc(spuVo,spuId);
        // 1.3. 保存spu的规格参数信息
        this.saveBaseAttr(spuVo, spuId);
        /// 2. 保存sku相关信息
        this.saveSku(spuVo,spuId);
        this.rabbitTemplate.convertAndSend("PMS_SPU_EXCHANGE","item.insert",spuId);

    }

    @Transactional
    public void saveSku(SpuVo spuVo,Long spuId) {
        List<SkuVo> skuVos = spuVo.getSkus();
        if (CollectionUtils.isEmpty(skuVos)){
            return;
        }
        skuVos.forEach(skuVo -> {
            // 2.1. 保存sku基本信息
            SkuEntity skuEntity = new SkuEntity();
            BeanUtils.copyProperties(skuVo,skuEntity);
            // 品牌和分类的id需要从spuInfo中获取
            skuEntity.setBrandId(spuVo.getBrandId());
            skuEntity.setCategoryId(spuVo.getCategoryId());
            //获取图片列表
            List<String> images = skuVo.getImages();
            // 如果图片列表不为null, 则设置默认图片
            if(!CollectionUtils.isEmpty(images)) {
                //设置第一张图片作为默认图片
                skuEntity.setDefaultImage(skuEntity.getDefaultImage() == null?images.get(0):skuEntity.getDefaultImage());
                skuEntity.setSpuId(spuId);
                this.skuMapper.insert(skuEntity);
                //获取skuId
                Long skuId = skuEntity.getId();
                // 2.2  保存sku图片信息
                if (!CollectionUtils.isEmpty(images)){
                    String defaultImage = images.get(0);
                    List<SkuImagesEntity> skuImages = images.stream().map(image -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setDefaultStatus(StringUtils.equals(defaultImage, image) ? 1 : 0);

                        skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setSort(0);
                        skuImagesEntity.setUrl(image);
                        return skuImagesEntity;
                    }).collect(Collectors.toList());
                    this.skuImagesService.saveBatch(skuImages);
                }

                // 2.3 保存sku的规格参数(销售属性)
                List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
                saleAttrs.forEach(saleAttr ->  {
                    saleAttr.setSort(0);
                    saleAttr.setSkuId(skuId);
                });
                this.skuAttrValueService.saveBatch(saleAttrs);
                // 3. 保存营销相关信息，需要远程调用gmall-sms

                SkuSaleVo skuSaleVo = new SkuSaleVo();
                BeanUtils.copyProperties(skuVo, skuSaleVo);
                skuSaleVo.setSkuId(skuId);
                this.gmallSmsClient.saveSkuSaleInfo(skuSaleVo);

            }

        });
    }

    @Transactional
    public void saveBaseAttr(SpuVo spuVo, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)) {
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueVo -> {
                spuAttrValueVo.setSpuId(spuId);
                spuAttrValueVo.setSort(0);
                return spuAttrValueVo;
            }).collect(Collectors.toList());
            this.baseService.saveBatch(spuAttrValueEntities);
        }
    }

     @Transactional
    public void saveSpuDesc(SpuVo spuVo, Long spuId) {
        SpuDescEntity spuInfoDescEntity  = new SpuDescEntity();
        // 注意：spu_info_desc表的主键是spu_id,需要在实体类中配置该主键不是自增主键
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(),","));
        this.descMapper.insert(spuInfoDescEntity);

    }

    @Transactional
    public Long saveSpu(SpuVo spuVo) {
        spuVo.setPublishStatus(1); // 默认是已上架
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime()); // 新增时，更新时间和创建时间一致
        this.save(spuVo);
        return spuVo.getId();
    }
}