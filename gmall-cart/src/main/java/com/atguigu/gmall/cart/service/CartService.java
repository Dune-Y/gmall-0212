package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptors.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class CartService {


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartAsyncService asyncService;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    public void saveCart(Cart cart) {
        // 1.获取登录状态
        String userId = getUserId();
        // 2.判断当前用户的购物车是否包含该商品 Map<userId/userKey, Map<skuId, cartJson>>
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        // Map<skuId, cartJson>
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount(); // 本次新增数量
        if (hashOps.hasKey(skuId)) {
            // 包含则更新数量
            String cartJson = hashOps.get(skuId).toString();
            // 反序列化为购物车对象
            cart = JSON.parseObject(cartJson, Cart.class);
            // 数据库中的数量累加新增的数量
            cart.setCount(cart.getCount().add(count));
            // 更新到数据库 redis mysql
            this.asyncService.update(userId, skuId, cart);
        } else {
            // 不包含则新增记录 skuId count
            cart.setUserId(userId);
            cart.setCheck(true);
            // 根据skuId查询sku
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new CartException("您要加入购物车的商品不存在！");
            }
            cart.setTitle(skuEntity.getTitle());
            cart.setPrice(skuEntity.getPrice());
            cart.setDefaultImage(skuEntity.getDefaultImage());
            // 根据skuId查询当前sku的销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrsResponseVo = this.pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrsResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));
            // 根据skuId查询库存
            ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
            // 根据skuId查询营销信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));
            // 保存到数据库 redis mysql
            this.asyncService.insert(userId,cart);
            // 添加实时价格缓存
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }
        hashOps.put(skuId, JSON.toJSONString(cart));
    }


    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = userInfo.getUserKey();
        if (userInfo.getUserId() != null) {
            userId = userInfo.getUserId().toString();
        }
        return userId;
    }

    public Cart queryCartBySkuId(Long skuId) {
        // 获取登录状态
        String userId = this.getUserId();
        // 内层的map<skuId, cartJson>
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (!hashOps.hasKey(skuId.toString())) {
            throw new CartException("您的购物车中没有该商品！");
        }
        String cartJson = hashOps.get(skuId.toString()).toString();
        return JSON.parseObject(cartJson, Cart.class);
    }

    //ListenableFuture 有返回结果集 非阻塞方式 有异常信息
    @Async
    public ListenableFuture<String> excute1() {
        try {
            System.out.println("execute1方法开始执行...");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("execute1方法结束执行...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return AsyncResult.forValue("hello execute1");
    }

    @Async
    public ListenableFuture<String> excute2() {
        try {
            System.out.println("execute1方法开始执行...");
            TimeUnit.SECONDS.sleep(4);
            System.out.println("execute1方法结束执行...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return AsyncResult.forValue("hello execute2");
    }

    public List<Cart> queryCarts() {

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        // 1.以userKey查询未登录的购物车
        String userKey = userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getUserKey());
        List<Object> cartJsons = unLoginHashOps.values();
        // 把未登录的购物车的json字符串 转化成 购物车对象集合
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(cartJsons)) {
            unLoginCarts = cartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                // 查询购物车时，查询实时价格缓存
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }
        // 2.判断是否登录（userId == null），如果未登录则直接返回未登录的购物车
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return unLoginCarts;
        }
        // 3.把未登录的购物车 合并到 已登录的购物车（userId）中去
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        // 如果存在未登录的购物车, 那么合并未登录车到已经登录的购物车
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {//遍历未登录购物车中的每一条记录
                // 判断已登录的购物车中是否包含该记录
                String skuId = cart.getSkuId().toString();
                BigDecimal count = cart.getCount(); // 未登录购物车中数量
                if (loginHashOps.hasKey(skuId)) {
                    // 如果包含则更新数量
                    String cartJson = loginHashOps.get(skuId).toString(); // 已登录购物车的json字符串
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    // 保存到数据库
                    this.asyncService.update(userId.toString(), skuId, cart);
                } else {
                    // 如果不包含则新增记录
                    cart.setId(null);
                    cart.setUserId(userId.toString());
                    this.asyncService.insert(userId.toString(),cart);
                }
                loginHashOps.put(skuId, JSON.toJSONString(cart));
            });
            // 4.清空未登录的购物车
            this.redisTemplate.delete(KEY_PREFIX + userKey);
            this.asyncService.deleteByUserId(userKey);
        }
        // 5.返回合并后的购物车给用户
        List<Object> loginCartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(loginCartJsons)) {
            return loginCartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }
        return null;
    }

    public void updateNum(Cart cart) {

        // 获取登录状态
        String userId = this.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        if (hashOps.hasKey(skuId)) {
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(skuId, JSON.toJSONString(cart));
            this.asyncService.update(userId, skuId, cart);
        }

    }

    public void deleteCart(Long skuId) {
        // 获取登录状态
        String userId = this.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        hashOps.delete(skuId.toString());
        this.asyncService.deleteByUserIdAndSkuId(userId, skuId);
    }


    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> cartJsons = hashOps.values();
        if (!CollectionUtils.isEmpty(cartJsons)) {
            return cartJsons.stream()
                    .map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class))
                    .filter(Cart::getCheck).collect(Collectors.toList());
        }
        throw new CartException("您的购物车为空！");
    }


    public void checkCart(Cart cart) {
        // 获取登录状态
        String userId = this.getUserId();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        String skuId = cart.getSkuId().toString();
        Boolean check = cart.getCheck();
        if (hashOps.hasKey(skuId)) {
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCheck(check);
            hashOps.put(skuId, JSON.toJSONString(cart));
            this.asyncService.update(userId, skuId, cart);
        }
    }


}
