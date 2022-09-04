package com.atguigu.gmall.index.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 指定缓存的前缀。默认：gmall:cache:
     * @return
     */
    String prefix() default "gmall:cache:";

    /**
     * 指定缓存的过期时间。默认：30min
     * 单位：min
     * @return
     */
    int timeout() default 30;
    /**
     * 为了防止缓存雪崩，可以给缓存添加一个随机值
     * 这里可以指定随机值的范围，默认：10min
     * @return
     */
    int random() default 10;

    /**
     * 为了防止缓存击穿，给缓存添加分布式锁
     * 这里可以指定分布式锁的前缀。分布式锁格式：lock前缀 + 方法形参
     * @return
     */
    String lock() default "gmall:lock:";


}
