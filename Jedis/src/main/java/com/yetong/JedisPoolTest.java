package com.yetong;

import redis.clients.jedis.Jedis;

public class JedisPoolTest {

    public static void main(String[] args) {
        Jedis jedis = JedisPoolFactory.getJedis();
        jedis.select(0);
        jedis.set("user","是一个老师");
        System.out.println(jedis.get("user"));
        if (jedis!=null){
            jedis.close();
        }
    }

}
