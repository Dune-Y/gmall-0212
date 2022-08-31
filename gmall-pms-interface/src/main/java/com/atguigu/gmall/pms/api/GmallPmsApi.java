package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
    @PostMapping("pms/spu/json")
    ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo pageParamVo);
    @GetMapping("pms/spu/{id}")
    ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);
    @GetMapping("pms/sku/spu/{spuId}")
    ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId")Long spuId);
    @GetMapping("pms/brand/{id}")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("pms/category/parent/{parentId}")
    ResponseVo<List<CategoryEntity>> queryCategoryByPId(@PathVariable("parentId")Long pid);

    @GetMapping("pms/category/level23/{pid}")
    ResponseVo<List<CategoryEntity>> queryLvl23CategoriesByPid(@PathVariable("pid") Long pid);

    @GetMapping("pms/skuattrvalue/search/attr/value/{cid}")
    ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueByCidAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("skuId")Long skuId
    );

    @GetMapping("pms/spuattrvalue/search/attr/value/{cid}")
    ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValueByCidAndSpuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId
    );
}
