package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Test
    void contextLoads() {

        IndexOperations indexOps = this.restTemplate.indexOps(Goods.class);
        if (!indexOps.exists()){
        indexOps.create();
        indexOps.putMapping(indexOps.createMapping());
        }

        Integer pageNum = 1;
        Integer pageSize = 100;

        do {
            // 分批查询spu
            ResponseVo<List<SpuEntity>> spuResponseVo = this.pmsClient.querySpuByPageJson(new PageParamVo(pageNum, pageSize, null));
            List<SpuEntity> spuEntities = spuResponseVo.getData();
            if (CollectionUtils.isEmpty(spuEntities)) {
                return;
            }
            // 遍历当前页的spu，查询spu下的sku 抓化成goods集合 导入到es索引库
            spuEntities.forEach(spuEntity -> {
                ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkusBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();
                // 判断当前spu下的sku是否为空，如果不为空则转化成goods集合
                if (!CollectionUtils.isEmpty(skuEntities)) {
                    // 查询品牌
                    ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(spuEntity.getBrandId());
                    BrandEntity brandEntity = brandEntityResponseVo.getData();
                    // 查询表分类
                    ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(spuEntity.getCategoryId());
                    CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                    // 查询基本类型的检索属性和值
                    ResponseVo<List<SpuAttrValueEntity>> baseAttrResponseVo = this.pmsClient.querySearchAttrValueByCidAndSpuId(spuEntity.getCategoryId(), spuEntity.getId());
                    List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrResponseVo.getData();


                    List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();

                        // sku相关信息
                        goods.setTitle(skuEntity.getTitle());
                        goods.setSubtitle(skuEntity.getSubtitle());
                        goods.setDefaultImage(skuEntity.getDefaultImage());
                        goods.setPrice(skuEntity.getPrice().doubleValue());
                        goods.setSkuId(skuEntity.getId());

                        // 设置创建时间
                        goods.setCreateTime(spuEntity.getCreateTime());

                        // 销量及是否有货
                        ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                            goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                            goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                        }
                        // 品牌
                        if (brandEntity != null) {
                            goods.setBrandId(brandEntity.getId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }

                        // 分类
                        if (categoryEntity != null) {
                            goods.setCategoryId(categoryEntity.getId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        // 检索类型的规格参数
                        ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = this.pmsClient.querySearchAttrValueByCidAndSkuId(skuEntity.getCategoryId(), skuEntity.getId());
                        List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();

                        List<SearchAttrValueVo> searchAttrs = new ArrayList<>();

                        // 把销售类型的检索属性和值 转化成 vo对象
                        if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                            List<SearchAttrValueVo> searchAttrValueVos = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                SearchAttrValueVo attrValueVo = new SearchAttrValueVo();
                                BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                                return attrValueVo;
                            }).collect(Collectors.toList());
                            searchAttrs.addAll(searchAttrValueVos);
                        }
                        // 把基本类型的检索属性和值 转化成 vo对象
                        if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                            List<SearchAttrValueVo> searchAttrValueVos = spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                SearchAttrValueVo attrValueVo = new SearchAttrValueVo();
                                BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                                return attrValueVo;
                            }).collect(Collectors.toList());
                            searchAttrs.addAll(searchAttrValueVos);
                        }
                        goods.setSearchAttrs(searchAttrs);
                        return goods;
                    }).collect(Collectors.toList());
                    this.restTemplate.save(goodsList);
                }
            });
            pageSize = spuEntities.size();
            pageNum++;
        } while (pageSize == 100);
    }
}