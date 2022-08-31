package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @ ClassName IndexController
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/8/31 11:00
 * @ Version 1.0
 */

@Controller
public class IndexController {

    @Autowired
    IndexService indexService;

    @GetMapping("/**")
    public String toIndex(Model model){
      List<CategoryEntity> categoryEntityList = this.indexService.queryLvl1Categories();
      model.addAttribute("categories",categoryEntityList);
      return "index";
    }

    @GetMapping("/index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLv123CategoriesByPid(@PathVariable("pid") Long pid) {
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl23CategoriesByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("index/test/lock")
    @ResponseBody
    public ResponseVo testLock(){
        this.indexService.testLock();
        return ResponseVo.ok();
    }
}
