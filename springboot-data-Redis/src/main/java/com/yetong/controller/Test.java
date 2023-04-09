package com.yetong.controller;

import com.alibaba.fastjson2.JSON;
import com.yetong.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 使用的都是StringSerizalie
     * 需要手动序列化和发序列化
     */
    @Autowired
    StringRedisTemplate rt;

    @RequestMapping("/test")
    public void test(){
        redisTemplate.opsForValue().set("user2","数学老师");
        System.out.println(redisTemplate.opsForValue().get("user2"));
    }


    @RequestMapping("/test2")
    public void test2(){
        rt.opsForValue().set("user3",JSON.toJSONString(new User("12","小明")));
        System.out.println(JSON.parseObject(rt.opsForValue().get("user3"), User.class));
        System.out.println(redisTemplate.opsForValue().get("user2"));
    }

    @RequestMapping("/test3")
    public void test3(){
        rt.opsForHash().put("user:2","age","18");
        rt.opsForHash().put("user:2","name","小花");
        System.out.println(rt.opsForHash().get("user:2", "age"));
        System.out.println(rt.opsForHash().entries("user:2"));
        System.out.println(rt.opsForHash().entries("user:2").get("age"));
        System.out.println(JSON.parseObject(rt.opsForValue().get("user3"), User.class));
        System.out.println(redisTemplate.opsForValue().get("user2"));
    }
}
