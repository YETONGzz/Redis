package com.hmdp.utils;

import com.hmdp.config.JWTConfig;
import com.hmdp.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {

    @Value("${jwt.subject}")
    private String subject;
    @Value("${jwt.issUser}")
    private String issUser;

    public String createAccessToken(User user) {
        String token = Jwts.builder()
                .setId(String.valueOf(user.getId()))//设置id
                .setSubject(subject)//设置主题
                .setIssuedAt(new Date())//设置签发时间
                .setIssuer(issUser)//签发者
                .setExpiration(new Date(System.currentTimeMillis() + JWTConfig.expireTime))//设置过期时间
                .signWith(SignatureAlgorithm.HS256, JWTConfig.secret)//设置加密算法
                .claim("name", "夜瞳")//自定义其他属性
                .compact();
        return JWTConfig.prefix + token;
    }
}
