package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @ ClassName GmallCacheAspect
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/3 13:22
 * @ Version 1.0
 */



@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
   private StringRedisTemplate redisTemplate;


    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter bloomFilter;

//    @Pointcut("execution(* com.atguigu.gmall.index.service.*.*(..))")
    @Pointcut("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public void pointcut(){}

    /*
    * 1. 方法必须返回Object参数
    * */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 方法的签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 方法对象
        Method method = signature.getMethod();
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);

        //方法形参
        Object[] args = joinPoint.getArgs();
        //获取缓存前缀
        String prefix = gmallCache.prefix();
        String argString = StringUtils.join(args, ",");
        //缓存的key
        String key =  prefix + argString;

        //通过布隆过滤器判断数据是否存在, 不存在则直接返回空
        if (!bloomFilter.contains(key)){
            return null;
        }

        // 1. 先查询缓存, 如果缓存命中则直接返回
        String json = this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)) {
            return JSON.parseObject(json,signature.getReturnType());
        }

        // 2. 为了防止缓存击穿 ,添加分布式锁
        String lock = gmallCache.lock() + argString;
        RLock fairLock = redissonClient.getFairLock(lock);
        fairLock.lock();

        // 3. 当前请求获取锁的过程中, 可能有其他请求已经把数据放入缓存, 此时可以再次查询缓存, 如果命中则直接返回
        try {
            String json2 = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(json2)) {
                return JSON.parseObject(json2,signature.getReturnType());
            }
            // 4. 执行目标方法调用查询数据库
            Object result = joinPoint.proceed(args);
            // 5. 把数据放入redis对应的缓存中
            // 指定缓存时间 防止缓存雪崩
            int timeout = gmallCache.timeout() + new Random().nextInt(gmallCache.random());
            redisTemplate.opsForValue().set(key,JSON.toJSONString(result),timeout, TimeUnit.MINUTES);
            return result;
        }finally {
            fairLock.unlock();
        }
    }


    /*    @Before("pointcut()")
    public void before() {
        System.out.println("这是前置增强");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("这是后置增强");
    }*/
}
