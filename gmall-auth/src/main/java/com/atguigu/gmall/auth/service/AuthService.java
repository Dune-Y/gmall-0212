package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.AuthException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthService {



    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtProperties properties;
    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response)  {
        // 1.根据登录名和密码调用ums的接口查询用户
        ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();
        // 2.判空，如果为空说明登录名或者密码错误则抛出异常
        if (userEntity == null) {
            throw new AuthException("您的用户名或者密码错误！");
        }
        // 3.组装载荷
        Map<String, Object> map = new HashMap<>();
        map.put("userId",userEntity.getId());
        map.put("username",userEntity.getUsername());
        // 为了防止盗用，在载荷中加入登录用户的ip地址
        map.put("ip", IpUtils.getIpAddressAtService(request));
        try {
            // 4.生成jwt类型的token
            String token = JwtUtils.generateToken(map, this.properties.getPrivateKey(), this.properties.getExpire());
            // 5.放入cookie
            CookieUtils.setCookie(request,response,this.properties.getCookieName(),token,this.properties.getExpire() * 60);
            // 6.昵称展示
            CookieUtils.setCookie(request, response, this.properties.getUnick(), userEntity.getNickname(), this.properties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
