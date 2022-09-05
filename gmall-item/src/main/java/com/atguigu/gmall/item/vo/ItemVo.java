package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @ ClassName ItemVo
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/4 13:27
 * @ Version 1.0
 */
@Data
public class ItemVo {
    // 面包屑所需字段  V
    private List<CategoryEntity> categories;
    // 品牌 V
    private Long brandId;
    private String brandName;
    // spu信息 V
    private Long spuId;
    private String spuName;

    // 基本信息  V
    private Long skuId;
    private String title;
    private String subtitle;
    private BigDecimal price;
    private Integer weight;
    private String defaultImage;

    private List<SkuImagesEntity> image; // sku图片列表 V

    private List<ItemSaleVo> sales; // 营销信息 V

    private Boolean store = false; // 是否有货 V

    // [{attrId: 3, attrName: 机身颜色, attrValues: ['白天白', '暗夜黑']},
    // {attrId: 4, attrName: 运行内存, attrValues: ['8G', '12G']},
    // {attrId: 5, attrName: 机身存储, attrValues: ['256G', '512G']}]
    private List<SaleAttrValueVo> saleAttrs; // 销售属性列表 V

    // 当前sku的销售属性：{3: 白天白, 4: 12G, 5: 256G}
    private Map<Long, String> saleAttr;

    // 销售属性组合与skuId的映射关系：V {'白天白,12G,256G': 100, '白天白,8G,256G': 101, '白天白,12G,512G': 102, '白天白,8G,512G': 103}
    private String skuJsons;

    // spu的描述信息
    private List<String> spuImages;

    // 规格参数分组
    private List<ItemGroupVo> groups;


}
