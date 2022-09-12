package com.atguigu.gmall.cart.interceptors;

import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * @ ClassName LoginInterceptor
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/9 18:57
 * @ Version 1.0
 */

@Component
//@Data
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor implements HandlerInterceptor {

    // private UserInfo userInfo;
    @Autowired
    private JwtProperties properties;
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("前置方法,在controller方法执行之前执行");

        // 从cookie中获取token和userKey
        String token = CookieUtils.getCookieValue(request, this.properties.getCookieName());
        String userKey = CookieUtils.getCookieValue(request, this.properties.getUserKey());
        // 不管又没有登录, userKey都应该存在
        if (StringUtils.isBlank(userKey)) {
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, this.properties.getUserKey(), userKey, this.properties.getExpire());
        }

        // 从token中解析出userId
        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, this.properties.getPublicKey());
            userId = Long.valueOf(map.get("userId").toString());
        }
        //已经获取了登录信息 userId userKey
        UserInfo userInfo = new UserInfo(userId, userKey);
        THREAD_LOCAL.set(userInfo);
        return true;
    }


    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("后置方法,在controller方法执行之后执行");

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //必须手动释放局部变量, 因为我们这里使用的tomcat线程池, 否则导致内存泄露
        THREAD_LOCAL.remove();
        System.out.println("完成方法, 在视图渲染之后执行");
    }
}
