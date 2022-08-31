package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Document(indexName = "goods",shards = 3, replicas = 2)
@Data
public class Goods {
    // sku相关字段
    @Id
    private Long skuId;
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Keyword, index = false)
    private String subtitle;
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImage;
    @Field(type = FieldType.Double)
    private Double price;

    // 排序所需的字段：
    @Field(type = FieldType.Long)
    private Long sales = 0L; // 销量排序所需字段
    @Field(type = FieldType.Date,format = DateFormat.date)
    private Date createTime; // 新品排序所需字段
    @Field(type = FieldType.Boolean)
    private Boolean store = false; // 是否有货的过滤字段

    /// 过滤所需字段：
    // 品牌所需的字段
    @Field(type = FieldType.Long)
    private Long brandId;
    @Field(type = FieldType.Keyword)
    private String brandName;
    @Field(type = FieldType.Keyword)
    private String logo;

    // 分类所需的字段：
    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Keyword)
    private String categoryName;

    // 规格参数过滤
    @Field(type = FieldType.Nested)
    private List<SearchAttrValueVo> searchAttrs;

}
