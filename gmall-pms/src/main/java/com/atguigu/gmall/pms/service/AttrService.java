package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrEntity;

import java.util.List;

/**
 * 商品属性
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2022-07-22 19:59:33
 */
public interface AttrService extends IService<AttrEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrEntity> queryAttrByCidOrTypeOrSearchType(Long cid, Integer type, Integer searchType);

}

