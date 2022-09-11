package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;

/**
 * @ ClassName CartController
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/9 19:30
 * @ Version 1.0
 */
@Controller
public class CartController {

    @GetMapping("test")
    @ResponseBody
    public String test() {
//        request.getAttribute("userId") + "=====" + request.getAttribute("userKey")
//        System.out.println("这是controller方法....." + LoginInterceptor.getUserInfo());
        long now = System.currentTimeMillis();
        System.out.println("controller方法开始执行");
        ListenableFuture<String> future1 = this.cartService.excute1();
        ListenableFuture<String> future2 = this.cartService.excute2();
        try {
            System.out.println(future1.get() + "===========" + future2.get());
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("controller方法结束执行==============" + (System.currentTimeMillis() - now));
        return "hello cart...";
    }

    @Autowired
    private CartService cartService;


    @GetMapping
    public String saveCart(Cart cart) {
        this.cartService.saveCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId() + "&count=" + cart.getCount();
    }


    @GetMapping("addCart.html")
    public String queryCart(Cart cart, Model model) {
        BigDecimal count = cart.getCount();
        cart = this.cartService.queryCartBySkuId(cart.getSkuId());
        cart.setCount(count);
        model.addAttribute("cart", cart);
        return "addCart";
    }








}
