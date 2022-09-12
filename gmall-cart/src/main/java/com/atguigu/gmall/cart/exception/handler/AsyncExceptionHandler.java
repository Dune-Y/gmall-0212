package com.atguigu.gmall.cart.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @ ClassName AsyncExceptionHandler
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/11 12:14
 * @ Version 1.0
 */

@Slf4j
@Component
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {


    private static final String EXCEPTION_KEY = "cart:exception";

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        // 记录日志 或者 记录到数据库
        // key 必须是一个固定的key Set<cart:exception,userId集合>
//        UserInfo userInfo = LoginInterceptor.getUserInfo();
        this.redisTemplate.boundSetOps(EXCEPTION_KEY).add(objects[0].toString());
        //  log.error("异步任务执行失败. 失败信息:{}, 方法:, 参数列表{}", throwable.getMessage(), method.getName(), Arrays.asList(objects));
    }
}
