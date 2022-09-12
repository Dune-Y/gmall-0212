package com.atguigu.gmall.scheduled.jobhandler;

import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.atguigu.gmall.scheduled.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;



@Component
public class CartJobHandler {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CartMapper cartMapper;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String EXCEPTION_KEY = "cart:exception";
    private static final String KEY_PREFIX = "cart:info:";


    @XxlJob("cartSyncData")
    public ReturnT<String> cartSyncData(String param) {
        // 1.从redis中读取异常信息（Set<UserId>）
        BoundSetOperations<String, String> setOps = this.redisTemplate.boundSetOps(EXCEPTION_KEY);
        // 2.遍历userId集合
        String userId = setOps.pop();
        while (StringUtils.isNotBlank(userId)) {
            // 3.删除该用户mysql中的所有购物车
            this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId));
            // 4.读取该用户redis中的购物车
            BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
            List<Object> cartJsons = hashOps.values();
            // 5.判断redis中的购物车是否为空，为空则结束
            if (CollectionUtils.isEmpty(cartJsons)){
                userId = setOps.pop();
                continue;
            }
            // 6.把redis中的数据新增到mysql
            cartJsons.forEach(cartJson -> {
                try {
                    Cart cart =  MAPPER.readValue(cartJson.toString(),Cart.class);
                    cart.setId(null);
                    this.cartMapper.insert(cart);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            });
            // 获取下一个用户
            userId = setOps.pop();
        }
        return ReturnT.SUCCESS;
    }
}
