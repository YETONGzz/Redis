package com.yetong.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Jwt配置
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JWTConfig {

    /**
     * 秘钥
     */
    public static String secret;

    /**
     * 授权key
     */
    public static String tokenHeader;

    /**
     * Token前缀
     */
    public static String prefix;

    /**
     * 过期时间
     */
    public static Integer expireTime;

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setTokenHeader(String tokenHeader) {
        this.tokenHeader = tokenHeader;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setExpireTime(Integer expireTime) {
        this.expireTime = expireTime;
    }
}
