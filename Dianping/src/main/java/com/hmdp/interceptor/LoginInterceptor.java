package com.hmdp.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.commond.CacheConstants;
import com.hmdp.commond.UserHolder;
import com.hmdp.config.JWTConfig;
import com.hmdp.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.commond.Constants.TOKEN_TIMEOUT;


public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 校验用户是否登陆,controller之前执行
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
        String key = CacheConstants.CODE_CACHE_PREFIX + token;
        if (!StringUtils.hasText(token)) {
            return false;
        }
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (CollectionUtils.isEmpty(map)) {
            return false;
        }
        UserDTO user = BeanUtil.toBean(map, UserDTO.class);
        redisTemplate.expire(key,TOKEN_TIMEOUT, TimeUnit.MINUTES);
        UserHolder.saveUser(user);
        return true;
    }


    /**
     * 视图渲染之后执行
     * 销毁保存在ThreadLocal中的用户信息
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
