package com.rain.usercenter.service;

import com.rain.usercenter.mapper.UserMapper;
import com.rain.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class InsertUserTest {

    @Resource
    private UserService userService;


    /**
     * 批量插入数据
     */

    @Test
    public void  doInsertUser(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM =100;

        //分十组计算机
        int j = 0;
        List<CompletableFuture<Void>> futuresList = new ArrayList<>();

        for (int i= 0;i<10; i++){
            System.out.println("运行啦");
            //多个线程同事插入userList会有问题，所以要转成安全的线程集合就好了
            List<User> userList = new ArrayList<>();

            while (true){
                j++;
                User user =  new User();
                user.setUsername("假用户");
                user.setUserAccount("Rain神");
                user.setUserPassword("12345678");
                user.setTags("[]");
                user.setAvatarUrl("https://tse2-mm.cn.bing.net/th/id/OIP-C.qcrd_3FD3HLkpktBPuVEMwAAAA?w=202&h=202&c=7&r=0&o=5&dpr=1.3&pid=1.7");
                user.setGender(0);
                user.setPhone("1234");
                user.setEmail("123@qq.com");
                user.setProfile("一名努力的打工人");
                user.setUserRole(0);
                user.setUserStatus(0);
                userList.add(user);
                if (j % 1000 ==0) {
                    break;
                }
            }
            //开启异步任务
            CompletableFuture<Void>future = CompletableFuture.runAsync(()->{
                userService.saveBatch(userList,1000);
            });
            //把是十个异步任务插入到futuresList
            futuresList.add(future);

        }
        //join（）的作用是等这个十个异步任务执行完之后才去执行下行的代码
        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }

}
