package com.rain.usercenter.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class RedissonTest {
@Resource
    private RedissonClient redissonClient;

@Test
    void test(){
    //list
    //这里是储存在jvm本地内存
    List<String> list = new ArrayList<>();
    list.add("rain");
   // list.remove(0);
    System.out.println("list:" +list.get(0));

    //这个是存储在redis
    RList<String>rList = redissonClient.getList("test-list");
    //rList.add("rain");
    System.out.println("rlist:" +rList.get(0));
    rList.remove(0);
    //map


    //set
}


}
