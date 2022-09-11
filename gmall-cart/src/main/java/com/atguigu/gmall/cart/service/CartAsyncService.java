package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @ ClassName CartAsyncService
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/11 12:49
 * @ Version 1.0
 */
@Service
public class CartAsyncService {


    @Autowired
    CartMapper cartMapper;

    @Async
    public void update(String userId, String skuId, Cart cart) {
        this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }


    @Async
    public void insert(Cart cart){
        cartMapper.insert(cart);
    }

}
