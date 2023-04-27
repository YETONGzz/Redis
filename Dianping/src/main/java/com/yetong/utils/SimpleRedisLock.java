package com.yetong.utils;

import cn.hutool.core.util.BooleanUtil;
import com.yetong.commond.RedisService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 自定义分布式锁
 */
public class SimpleRedisLock {

    private StringRedisTemplate redisTemplate;
    private RedisService redisService;
    private String key;
    private String lockFlagPrefix = String.valueOf(new SnowFlake().nextId());
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    private static final DefaultRedisScript<Long> LOCK_SCRIPT;
    private static final Long ACQUIRE_LOCK = 1L;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("luas/unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
        LOCK_SCRIPT = new DefaultRedisScript();
        LOCK_SCRIPT.setLocation(new ClassPathResource("luas/lock.lua"));
        LOCK_SCRIPT.setResultType(Long.class);
    }

    private SimpleRedisLock(String key, StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    private SimpleRedisLock(String key, RedisService redisService) {
        this.redisService = redisService;
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
        String threadFlag = lockFlagPrefix + Thread.currentThread().getId();
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, threadFlag, timeout, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }


    /**
     * 自定义分布式可重入锁
     *
     * @param timeout
     * @return
     */
    public boolean tryReentrantLock(Long timeout) {
        String threadFlag = lockFlagPrefix + Thread.currentThread().getId();
        Long execute = redisTemplate.execute(
                LOCK_SCRIPT,
                Collections.singletonList(key),
                threadFlag, Long.valueOf(TimeUnit.SECONDS.toMillis(timeout)).toString());
        return execute == ACQUIRE_LOCK;
    }

    public boolean tryLock(String key, Long timeout, TimeUnit unit) {
        String threadFlag = lockFlagPrefix + Thread.currentThread().getId();
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, threadFlag, timeout, unit);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁 自己的锁标识才可以释放
     */
    public void unLock() {
        //释放锁时判断锁标识  是自己的锁标识才可以释放锁
        redisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(key),
                lockFlagPrefix + Thread.currentThread().getId());
    }

    /**
     * 自定义释放可重入锁
     */
    public void unReentrantLock() {
        //释放锁时判断锁标识  是自己的锁标识才可以释放锁
        Long execute = redisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(key),
                lockFlagPrefix + Thread.currentThread().getId());
    }


}
