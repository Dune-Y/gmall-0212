package com.atguigu.gmall.item.vo;

import lombok.Data;

import java.util.List;

/**
 * @ ClassName SaleAttrValueVo
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/4 13:38
 * @ Version 1.0
 */
@Data
public class SaleAttrValueVo {
    private Integer attrId;
    private String attrName;
    private List<String> attrValues;
}
