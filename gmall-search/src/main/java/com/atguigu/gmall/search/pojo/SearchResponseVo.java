package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {

    // 过滤中的品牌列表
    private List<BrandEntity> brands;

    // 过滤中的分类列表
    private List<CategoryEntity> categories;

    // 过滤中的规格参数列表
    private List<SearchResponseAttrVo> filters;

    // 分页结果集
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<Goods> goodsList;

}
