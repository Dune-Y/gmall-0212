package com.atguigu.gmall.gateway.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * @ ClassName TestGatewayFilterFactory
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/8 17:50
 * @ Version 1.0
 */
@Component
public class TestGatewayFilterFactory extends AbstractGatewayFilterFactory<TestGatewayFilterFactory.KeyValueConfig> {

    public TestGatewayFilterFactory() {
        super(KeyValueConfig.class);
    }


    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public GatewayFilter apply(KeyValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("我是局部过滤器, 拦截特定路由对应的服务的请求!paths = " + config.paths) ;
                return chain.filter(exchange);
            }
        };
    }
    
    
    @Data
    public static class KeyValueConfig {
        private List<String> paths;
//        private String key;
//        private String value;
    }
}
