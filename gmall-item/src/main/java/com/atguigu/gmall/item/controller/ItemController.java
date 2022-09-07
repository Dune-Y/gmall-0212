package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @ ClassName ItemController
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/5 20:58
 * @ Version 1.0
 */


@Controller
public class ItemController {

    @Autowired
    ItemService itemService;

    @GetMapping("{skuId}.html")
    public String loadData(@PathVariable("skuId") Long skuId, Model model) {
        ItemVo itemVo = this.itemService.loadData(skuId);
        model.addAttribute("itemVo",itemVo);
        return "item";
    }
    // ctrl shift + f9

}
