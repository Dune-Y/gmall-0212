package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.uitils.DistribuitedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
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
    DistribuitedLock distribuitedLock;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:cates";
    private static final String LOCK_PREFIX = "index:cates:lock:";


    public List<CategoryEntity> queryLvl1Categories(){
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoryByPId(0L);
        return listResponseVo.getData();
    }

    //一次性把二级分类和三级分类都查出来

    @GmallCache(prefix = KEY_PREFIX,timeout = 129600, random = 14400, lock = LOCK_PREFIX)
    public List<CategoryEntity> queryLvl23CategoriesByPid(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryLvl23CategoriesByPid(pid);
        return listResponseVo.getData();
    }





    //一次性把二级分类和三级分类都查出来
    public List<CategoryEntity> queryLvl23CategoriesByPid2(Long pid) {


        // 1. 先查询缓存, 如果缓存命中则返回
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(json)){
            return JSON.parseArray(json, CategoryEntity.class);
        }
        //为了防止缓存击穿, 添加分布式锁
        RLock fairLock = this.redissonClient.getFairLock(LOCK_PREFIX + pid);
        fairLock.lock();
        try {
            // 当前请求获取锁的过程中, 可能有其他请求已经把数据放入缓存,此时,可以再次查询缓存, 如果命中则直接返回

            String json2 = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
            if(StringUtils.isNotBlank(json2)){
                return JSON.parseArray(json2, CategoryEntity.class);
            }
            // 2. 走远程调用并放入缓存
            ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryLvl23CategoriesByPid(pid);
            List<CategoryEntity> categoryEntities = listResponseVo.getData();
            // 解决缓存穿透的问题:数据为空的时候也进行缓存, (布隆过滤器是最优解决方案)
            if(CollectionUtils.isEmpty(categoryEntities)){
                this.redisTemplate.opsForValue().set(KEY_PREFIX+ pid,JSON.toJSONString(categoryEntities),5, TimeUnit.MINUTES);
            }else {
                this.redisTemplate.opsForValue().set(KEY_PREFIX+ pid,JSON.toJSONString(categoryEntities),90+ new Random().nextInt(10), TimeUnit.DAYS);
            }
            return categoryEntities;
        } finally {
            fairLock.unlock();
        }
    }


    public void testLock(){
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();
        try {
            String number = this.redisTemplate.opsForValue().get("number");
            if (StringUtils.isBlank(number)) {
                this.redisTemplate.opsForValue().set("number","1");
            }
            int num = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number",String.valueOf(++num));
        }finally {
            lock.unlock();
        }


    }


    //使用lua脚本实现了可重入锁
    public void testLock3(){
        String uuid = UUID.randomUUID().toString();
        Boolean lock = this.distribuitedLock.tryLock("lock", uuid, 30);
        if (lock){
            String number = this.redisTemplate.opsForValue().get("number");
            if (StringUtils.isBlank(number)) {
                this.redisTemplate.opsForValue().set("number","1");
            }
            int num = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number",String.valueOf(++num));
            this.testSubLock(uuid);
            this.distribuitedLock.unLock("lock",uuid);
        }
    }

    // 1. 使用synchronized本地锁
    // 2. 使用redis锁
    public void testLock2(){
        String uuid = UUID.randomUUID().toString();
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent("lock",uuid,300,TimeUnit.SECONDS);
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
            this.testSubLock();
            // 解锁: 解锁之前先判断是否是自己的锁,如果是自己的锁才能解锁
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList("lock"),uuid);
//            if (StringUtils.equals(uuid,this.redisTemplate.opsForValue().get("lock"))){
//                this.redisTemplate.delete("lock");
//            }
        }
    }


    //测试可重入锁
    public void testSubLock(String uuid) {
        this.distribuitedLock.tryLock("lock",uuid,30);
        System.out.println("===================");
        this.distribuitedLock.unLock("lock",uuid);
    }

    public void testSubLock(){
        String uuid = UUID.randomUUID().toString();
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (!flag) {
            try {
                // 加锁失败,重试
                Thread.sleep(30);
                this.testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList("lock"),uuid);
    }

    public void testRead() {
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwLock");
        rwLock.readLock().lock(10,TimeUnit.SECONDS);
    }

    public void testWrite() {
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwLock");
        rwLock.writeLock().lock(10,TimeUnit.SECONDS);
    }


    public void testLatch() {
        try {
            RCountDownLatch cdl = this.redissonClient.getCountDownLatch("cdl");
            cdl.trySetCount(6);
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testCountDown() {
        RCountDownLatch cdl = this.redissonClient.getCountDownLatch("cdl");
        cdl.countDown();
        //出来了一位同学
    }
}
