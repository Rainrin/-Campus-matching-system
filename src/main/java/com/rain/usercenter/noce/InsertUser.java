package com.rain.usercenter.noce;


import com.rain.usercenter.mapper.UserMapper;
import com.rain.usercenter.model.domain.User;
import com.rain.usercenter.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Date;


@Component
public class InsertUser {

    @Resource
    private UserMapper userMapper;


    /**
     * 批量插入数据
     */
    public void  doInsertUser(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();


        final int INSERT_NUM =100;
        for (int i= 0 ;i<INSERT_NUM;i++){
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

            userMapper.insert(user);
        }

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }





}
