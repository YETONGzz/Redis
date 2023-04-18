package com.yetong.commond;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.yetong.utils.CacheService;
import com.yetong.utils.ParamUtil;
import com.yetong.utils.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.yetong.commond.CacheConstants.*;

@Slf4j
@Component
public class RedisService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    CacheService cacheService;
    private static final ExecutorService CACHE_EXECUTOR = Executors.newFixedThreadPool(10);

    public void set(String key, Object obj) {
        redisTemplate.opsForValue().set(key, JSON.toJSONString(obj));
    }

    public void set(String key, Object obj, Long expire, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, JSON.toJSONString(obj), expire, unit);
    }

    public <T> T get(String key, Class<T> tClass) {
        String json = redisTemplate.opsForValue().get(key);
        return JSON.parseObject(json, tClass);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void put(String key, Object obj) {
        Map map = ParamUtil.beanToMap(obj);
        redisTemplate.opsForHash().putAll(key, map);
    }

    public void put(String key, Object obj, Long expire, TimeUnit unit) {
        Map map = ParamUtil.beanToMap(obj);
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, expire, unit);
    }

    public Map getHash(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public <T> T getHash(String key, Class<T> tClass) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return BeanUtil.toBean(entries, tClass);
    }

    /**
     * 缓存穿透
     */
    public <T> T cachePenetrate(String key, Class<T> tClass, Supplier<T> supplier, Long expire, TimeUnit unit) {
        if (CACHE_BREAK.equals(get(key))) {
            return null;
        }
        String json = null;
        if (StringUtils.hasText(json = get(key))) {
            return JSON.parseObject(json, tClass);
        }
        T t = supplier.get();
        if (t == null) {
            set(key, CACHE_BREAK, KEY_TIMEOUT, PENETRATE_TIMEUNIT);
            return null;
        }
        set(key, t, expire, unit);
        return t;
    }


    /**
     * 互斥锁解决缓存击穿
     */
    public <T> T cacheBreakByMutex(String key, Class<T> tClass, Supplier<T> supplier, Long sleepTime, Long expire, TimeUnit unit) {
        if (CACHE_BREAK.equals(get(key))) {
            return null;
        }
        String json = null;
        if (StringUtils.hasText(json = get(key))) {
            return JSON.parseObject(json, tClass);
        }

        boolean flag = true;
        T t = null;
        try {
            //未获取到锁
            if (!cacheService.tryLock(key)) {
                flag = !flag;
                Thread.sleep(TimeUnit.MINUTES.toMillis(sleepTime));
                //方法重试
                return cacheBreakByMutex(key, tClass, supplier, sleepTime, expire, unit);
            }

            t = supplier.get();
            if (t == null) {
                set(key, CACHE_BREAK, KEY_TIMEOUT, PENETRATE_TIMEUNIT);
                return null;
            }
            set(key, t, expire, unit);
        } catch (Exception e) {
            log.error("缓存重建失败", e);
            throw new RuntimeException(e);
        } finally {
            if (flag) cacheService.unLock(key);
        }
        return t;
    }


    /**
     * 逻辑过期解决缓存击穿
     */
    public <T> T cacheBreakByLogicExpire(String key, Class<T> tClass, Supplier<T> supplier,Long timeout,TimeUnit timeUnit) {
        String json = get(key);
        T t = null;
        //判断是否是不存在的数据或者空数据
        if (CACHE_BREAK.equals(json) || !StringUtils.hasText(json)) {
            return null;
        }
        RedisData redisData = JSON.parseObject(json, RedisData.class);
        t = JSON.parseObject(JSON.toJSONString(redisData.getData()), tClass);
        //如果没有过期直接返回数据
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            return t;
        }

        //获取到锁异步进行缓存重建
        if (cacheService.tryLock(key,timeout,timeUnit)) {
            CACHE_EXECUTOR.submit(() -> {
                try {
                    T r = supplier.get();
                    if (r == null) {
                        set(key, CACHE_BREAK, KEY_TIMEOUT, PENETRATE_TIMEUNIT);
                    }
                    redisData.setData(r);
                    redisData.setExpireTime(LocalDateTime.now().plusMinutes(LOGIC_TIMEOUT));
                    set(key, redisData);
                } catch (Exception e) {
                    log.error("缓存重建失败", e);
                } finally {
                    cacheService.unLock(key);
                }
            });
        }
        return t;
    }
}
