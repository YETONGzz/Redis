package com.yetong;

import com.yetong.commond.CacheConstants;
import com.yetong.commond.RedisData;
import com.yetong.commond.RedisIdWorker;
import com.yetong.commond.RedisService;
import com.yetong.service.IShopService;
import com.yetong.utils.SnowFlake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class HmDianPingApplicationTests {

    @Autowired
    RedisService redisService;
    @Autowired
    IShopService shopService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedisIdWorker idWorker;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(30);


    @Test
    public void te() {
        RedisData redisData = new RedisData();
        redisData.setData(shopService.getById(1));
        redisData.setExpireTime(LocalDateTime.now().plusMinutes(30));
        redisService.set(CacheConstants.SHOP_CACHE + 1, redisData);
    }

    @Test
    public void teId() {
        Long a = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - 1640995200L;
        System.out.println(a);
        System.out.println(a << 32 | 1);
        for (int i = 0; i < 10; i++) {
            Long key = redisTemplate.opsForValue().increment("key");
            System.out.println(key);
        }


    }

    @Test
    public void testTime() throws Exception{
        CountDownLatch latch = new CountDownLatch(300);
        SnowFlake snowFlake = new SnowFlake();
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                //idWorker.nextId("test");
                System.out.println(snowFlake.nextId());
            }
            latch.countDown();
        };
        long l = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            EXECUTOR_SERVICE.submit(task);
        }
        latch.await();
        System.out.println(System.currentTimeMillis() - l);
    }


}
