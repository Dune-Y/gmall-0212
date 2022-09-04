package com.atguigu.gmall.index.config;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @ ClassName BloomFilterConfig
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/4 10:53
 * @ Version 1.0
 */
@Configuration
public class BloomFilterConfig {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GmallPmsClient pmsClient;

    private static final String KEY_PREFIX = "index:cates";

    @Bean
    public RBloomFilter bloomFilter() {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("index:bf");

        //必要的话可以创建定时任务
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                bloomFilter.tryInit(2000, 0.03);
                // 向布隆过滤器中初始化数据: 分类, 广告
                ResponseVo<List<CategoryEntity>> responseVo = pmsClient.queryCategoryByPId(0L);
                List<CategoryEntity> categoryEntities = responseVo.getData();
                if(!CollectionUtils.isEmpty(categoryEntities)){
                    categoryEntities.forEach(categoryEntity -> {
                        bloomFilter.add(KEY_PREFIX + categoryEntity.getId());
                    });
                }
            }
        },3000,60*60*1000);
        return bloomFilter;
    }
}
