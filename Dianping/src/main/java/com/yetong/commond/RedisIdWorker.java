package com.yetong.commond;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * redis全局唯一id生成器
 */
@Component
public class RedisIdWorker {

    /**
     * 起始时间戳
     */
    private static final Long BEGIN_TIME = 1681649667L;

    /**
     * 序列位数
     */
    private static final int SEQUENCE_BIT = 32;

    @Autowired
    StringRedisTemplate redisTemplate;

    public Long nextId(String prefix) {
        LocalDateTime now = LocalDateTime.now();
        //当前时间戳
        long currentTimeStamp = now.toEpochSecond(ZoneOffset.UTC);
        //最终31位时间戳
        long timeStamp = currentTimeStamp - BEGIN_TIME;
        String day = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long sequenceValue = redisTemplate.opsForValue().increment(CacheConstants.ID_PREFIX + prefix + ":" + day);

        return timeStamp << SEQUENCE_BIT | sequenceValue;
    }
}
