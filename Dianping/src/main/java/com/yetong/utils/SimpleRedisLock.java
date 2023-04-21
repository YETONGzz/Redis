package com.yetong.utils;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class SimpleRedisLock {

    private final StringRedisTemplate redisTemplate;
    private String key;
    private static Long threadId = Thread.currentThread().getId();

    private SimpleRedisLock(String key, StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    public static SimpleRedisLock getRedisLock(String key, StringRedisTemplate redisTemplate) {
        return new SimpleRedisLock(key, redisTemplate);
    }

    /**
     * 判断是否获取到锁
     *
     * @param timeout
     * @return
     */
    public boolean tryLock(long timeout) {
        //通过线程id实现可重入锁
        String threadFlag = threadId + key;
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, threadFlag, timeout, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    public boolean tryLock(String key, Long timeout, TimeUnit unit) {
        String threadFlag = threadId + key;
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, threadFlag, timeout, unit);
        return BooleanUtil.isTrue(flag);
    }

    public void unLock() {
        redisTemplate.delete(key);
    }
}
