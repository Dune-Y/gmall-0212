package com.atguigu.gmall.gateway.filter;

import org.springframework.stereotype.Component;

/**
 * @ ClassName MyGlobalFilter
 * @ Description  这是一个全局网关过滤器
 * @ Author Nimodo
 * @ Date 2022/9/8 17:31
 * @ Version 1.0
 */
@Component
public class MyGlobalFilter  {
//implements GlobalFilter
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        System.out.println("我是全局过滤器,拦截所有经过网关的请求");
//
//        // 放行
//        return chain.filter(exchange);
//    }
}
