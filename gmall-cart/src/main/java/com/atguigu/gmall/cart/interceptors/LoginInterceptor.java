package com.atguigu.gmall.cart.interceptors;

import com.atguigu.gmall.cart.pojo.UserInfo;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @ ClassName LoginInterceptor
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/9 18:57
 * @ Version 1.0
 */

@Component
@Data
public class LoginInterceptor implements HandlerInterceptor {

    private UserInfo userInfo;

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("前置方法,在controller方法执行之前执行");
         this.userInfo = new UserInfo(1L, UUID.randomUUID().toString());
//        request.setAttribute("userId", 1L);
//        request.setAttribute("userKey", UUID.randomUUID().toString());
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
