package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.Set;

/**
 * @ ClassName SaleAttrValueVo
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/4 13:38
 * @ Version 1.0
 */
@Data
public class SaleAttrValueVo {
    private Long attrId;
    private String attrName;
    private Set<String> attrValue;
}
