package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("controller方法结束执行==============" + (System.currentTimeMillis() - now));
        return "hello cart...";
    }

    @Autowired
    private CartService cartService;

    @GetMapping("checked/carts/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId") Long userId) {
        List<Cart> carts = this.cartService.queryCheckedCartsByUserId(userId);
        return ResponseVo.ok(carts);
    }

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

    @GetMapping("cart.html")
    public String queryCarts(Model model) {
        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts", carts);
        return "cart";
    }

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart) {
        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @PostMapping("updateStatus")
    @ResponseBody
    public ResponseVo checkCart(@RequestBody Cart cart) {
        this.cartService.checkCart(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId") Long skuId) {
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

}
