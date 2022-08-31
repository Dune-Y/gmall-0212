package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @ ClassName IndexService
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/8/31 11:12
 * @ Version 1.0
 */
@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "index:cates";


    public List<CategoryEntity> queryLvl1Categories(){
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoryByPId(0L);
        return listResponseVo.getData();
    }

    //一次性把二级分类和三级分类都查出来
    public List<CategoryEntity> queryLvl23CategoriesByPid(Long pid) {
        // 1. 先查询缓存, 如果缓存命中则返回
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(json)){
            return JSON.parseArray(json, CategoryEntity.class);
        }
        // 2. 走远程调用并放入缓存
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryLvl23CategoriesByPid(pid);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        // 解决缓存穿透的问题:数据为空的时候也进行缓存, (布隆过滤器)
        if(CollectionUtils.isEmpty(categoryEntities)){
            this.redisTemplate.opsForValue().set(KEY_PREFIX+ pid,JSON.toJSONString(categoryEntities),5, TimeUnit.MINUTES);
        }else {
            this.redisTemplate.opsForValue().set(KEY_PREFIX+ pid,JSON.toJSONString(categoryEntities),90+ new Random().nextInt(10), TimeUnit.DAYS);
        }
        return categoryEntities;
    }

    // 1. 使用synchronized本地锁
    // 2. 使用redis锁
    public void testLock(){
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent("lock", "111");
        if (!flag) {
            try {
                // 加锁失败,重试
                Thread.sleep(30);
                this.testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {

            String number = this.redisTemplate.opsForValue().get("number");
            if (StringUtils.isBlank(number)){
                this.redisTemplate.opsForValue().set("number","1");
            }
            // 逻辑漏洞会导致timeout问题(属于严重漏洞)
            int num = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number",String.valueOf(++num));
            // 解锁
            this.redisTemplate.delete("lock");
        }
    }
}
