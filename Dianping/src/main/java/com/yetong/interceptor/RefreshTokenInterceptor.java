package com.yetong.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.yetong.commond.CacheConstants;
import com.yetong.commond.UserHolder;
import com.yetong.config.JWTConfig;
import com.yetong.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yetong.commond.Constants.TOKEN_TIMEOUT;

/**
 * token续签拦截器
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * token刷新 向ThreadLocal中射入用户
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
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
        redisTemplate.expire(key, TOKEN_TIMEOUT, TimeUnit.MINUTES);
        UserHolder.saveUser(userDTO);
        return true;
    }
}
