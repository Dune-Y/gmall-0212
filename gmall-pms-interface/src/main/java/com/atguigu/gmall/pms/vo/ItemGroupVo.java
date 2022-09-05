package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @ ClassName ItemGroupVo
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/4 13:47
 * @ Version 1.0
 */
@Data
public class ItemGroupVo {
    private Long id;
    private String name;
    private List<AttrValueVo> attrs;
}
