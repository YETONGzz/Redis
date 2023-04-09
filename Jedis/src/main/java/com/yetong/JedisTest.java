package com.yetong;

import redis.clients.jedis.Jedis;

import java.util.HashMap;

public class JedisTest {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("192.168.18.130", 6379);
        //选择0号数据库
        jedis.select(0);
        jedis.set("user", "djhsalkjldksa");
        jedis.hset("user:1",new HashMap(){{
            put("one","123");
            put("two","234");
        }});
        System.out.println(jedis.hget("user:1","one"));
        System.out.println(jedis.hgetAll("user:1"));
        System.out.println(jedis.get("user"));
        if (jedis != null) {
            jedis.close();
        }
    }
}
