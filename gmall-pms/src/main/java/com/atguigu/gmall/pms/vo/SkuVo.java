package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ ClassName SkuVo
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/7/29 15:21
 * @ Version 1.0
 */
@Data
public class SkuVo extends SkuEntity {
    private List<String> images;
    // 积分活动
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;
    // 满减活动
    private Integer fullCount;
    private BigDecimal discount;
    // 是否叠加其他优惠[0-不可叠加,1-可叠加]
    private Integer addOther;
    private List<SkuAttrValueEntity> saleAttrs;

}
