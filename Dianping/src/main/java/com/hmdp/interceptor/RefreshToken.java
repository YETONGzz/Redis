package com.hmdp.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.commond.CacheConstants;
import com.hmdp.commond.UserHolder;
import com.hmdp.config.JWTConfig;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.commond.Constants.TOKEN_TIMEOUT;

/**
 * token续签拦截器
 */
public class RefreshToken implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    public RefreshToken(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(JWTConfig.tokenHeader);
        if (!StringUtils.hasText(token)) {
            return true;
        }
        String key = CacheConstants.CODE_CACHE_PREFIX + token;
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (CollectionUtils.isEmpty(map)) {
            return true;
        }
        UserDTO userDTO = BeanUtil.fillBeanWithMap(map, new UserDTO(), false);
        redisTemplate.expire(key,TOKEN_TIMEOUT, TimeUnit.MINUTES);
        UserHolder.saveUser(userDTO);
        return true;
    }
}
