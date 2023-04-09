package com.yetong;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolFactory {

    private static final JedisPool jedisPool;

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //最大连接数量
        jedisPoolConfig.setMaxTotal(10);
        //最大空闲数量
        jedisPoolConfig.setMaxIdle(10);
        //设置最小空闲数量  最少有几个连接是空闲的剩下的都被释放掉
        jedisPoolConfig.setMinIdle(0);
        // 创建连接池对象，参数：连接池配置、服务端ip、服务端端口、超时时间
        jedisPool = new JedisPool(jedisPoolConfig, "192.168.18.130", 6379, 1000);
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
}
