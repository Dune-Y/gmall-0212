package com.atguigu.gmall.index;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class GmallIndexApplicationTests {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
//        redisTemplate.opsForValue().set("name","柳岩");
//        String name = redisTemplate.opsForValue().get("name");
//        System.out.println(name);
    }

}
