package com.atguigu.gmall.index.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ ClassName RedissionConfig
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/3 9:49
 * @ Version 1.0
 */
@Configuration
public class RedissionConfig {

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.244.100:6379");
        return Redisson.create(config);
    }
}
