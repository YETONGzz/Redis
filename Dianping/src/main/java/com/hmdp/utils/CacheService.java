package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CacheService {

    private static final String RESOURCE = "1";

    @Autowired
    public StringRedisTemplate redisTemplate;

    /**
     * 判断是否获取到锁
     *
     * @param key
     * @return
     */
    public boolean tryLock(String key) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, RESOURCE);
        return BooleanUtil.isTrue(flag);
    }
}
