package com.yetong.utils;

import cn.hutool.core.util.BooleanUtil;
import com.yetong.commond.CacheConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, RESOURCE, CacheConstants.LOCK_TIMEOUT, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    public boolean tryLock(String key, Long timeout, TimeUnit unit) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, RESOURCE, timeout, unit);
        return BooleanUtil.isTrue(flag);
    }

    public void unLock(String key) {
        redisTemplate.delete(key);
    }
}
