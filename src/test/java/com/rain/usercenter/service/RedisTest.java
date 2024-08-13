package com.rain.usercenter.service;


import com.rain.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

@Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("RainString","gaveMe");
        valueOperations.set("RainInt",1);
        valueOperations.set("RainDouble",2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("rain");
        valueOperations.set("RainUserName",user);

        //查
        Object rain = valueOperations.get("RainString");
        Assertions.assertTrue("gaveMe".equals((String)rain));
        rain = valueOperations.get("RainInt");
        Assertions.assertTrue(1==(Integer)rain);
        rain = valueOperations.get("RainDouble");
        Assertions.assertTrue(2.0 == (Double)rain);
        System.out.println(valueOperations.get("RainUserName"));
        redisTemplate.delete("RainString");
    }



}
