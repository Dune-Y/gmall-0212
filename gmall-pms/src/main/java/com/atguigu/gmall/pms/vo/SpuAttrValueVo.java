package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;


import java.util.List;

/**
 * @ ClassName SpuAttrValueVo
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/7/29 15:16
 * @ Version 1.0
 */
public class SpuAttrValueVo extends SpuAttrValueEntity {
    public void setValueSelected(List<Object> valueSelected) {

        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected,","));
    }
}
