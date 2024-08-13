package com.rain.usercenter.service;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.rain.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public  class UserServiceTest {

    @Resource
    private UserService userService;


//    @Test
//    public  void testAddUser(){
////        User user = new User();
////        user.setUsername("dogYupi");
////        user.setUserAccount("123");
////        user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
////        user.setGender(0);
////        user.setUserPassword("xxx");
////        user.setPhone("123");
////        user.setEmail("456");
////        boolean result = userService.save(user);
////        System.out.println(user.getId());
////        assertTrue(result);
//    }

//    @Test
//    void userRegister() {
//        //测速不能为空
//        String userAccount = "rain";
//        String userPassword = "";
//        String checkPassword = "123456";
//        long result = userService.userRegister(userAccount,userPassword,checkPassword);
//        Assertions.assertEquals(-1,result);
//        //测试账户用户名小于4位
//        userAccount = "ra";
//        result = userService.userRegister(userAccount,userPassword,checkPassword);
//        Assertions.assertEquals(-1,result);
//        //测试密码小于8位
//        userAccount = "rain";
//        userPassword = "123456";
//        result = userService.userRegister(userAccount,userPassword,checkPassword);
//        Assertions.assertEquals(-1,result);
//        //测试特殊字符
//        userAccount ="ra in";
//        userPassword = "12345678";
//        result = userService.userRegister(userAccount,userPassword,checkPassword);
//        Assertions.assertEquals(-1,result);
//        //测试密码与校验密码是否一致
//        checkPassword = "123456789";
//        result = userService.userRegister(userAccount,userPassword,checkPassword);
//        Assertions.assertEquals(-1,result);
//        //用户名不能一致
//        userAccount = "DogRain";
//        checkPassword = "12345678";
//        result = userService.userRegister(userAccount,userPassword,checkPassword);
//        Assertions.assertEquals(-1,result);
//        //成功插入数据用户
//        userAccount = "rain";
//        result = userService.userRegister(userAccount,userPassword,checkPassword);
//        Assertions.assertEquals(-1,result);
//
//
//    }
@Test
   public void searchUserByTags(){
        List<String> tagNameList = Arrays.asList("Java", "python");
        List<User> userList = userService.searchUserByTags(tagNameList);
       Assert.assertNotNull(userList);
   }

}