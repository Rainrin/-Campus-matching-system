package com.rain.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rain.usercenter.common.BaseResponse;
import com.rain.usercenter.common.ErrorCode;
import com.rain.usercenter.common.ResulUtils;
import com.rain.usercenter.exception.BusinessException;
import com.rain.usercenter.model.domain.User;
import com.rain.usercenter.model.domain.request.UserLoginRequest;
import com.rain.usercenter.model.domain.request.UserRegisterRequest;
import com.rain.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.rain.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.rain.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController//接收到的数据都转换为json的一个注解
@RequestMapping("/user")
@CrossOrigin(origins ={"http://localhost:3000"} ,allowCredentials = "true")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @PostMapping("register")
    public BaseResponse <Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){

        if (userRegisterRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);

        return ResulUtils.success(result);
    }
    @PostMapping("login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){

        if (userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User result = userService.userLogin(userAccount, userPassword,request);
        return ResulUtils.success(result);
    }

    @PostMapping("logout")
    public BaseResponse<Integer> userLogOut( HttpServletRequest request){
        if (request == null){
            return null;
        }
        int result = userService.userLoginOut(request);
        return ResulUtils.success(result);
    }

    @PostMapping("/update")
    public  BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        }
        User loginUser = userService.getLoginUser(request);
     int result =  userService.updateUser(user,loginUser);
        return ResulUtils.success(result);
    }
//数据库表的展示(仅管理员可以看到)
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(String username, HttpServletRequest request){
        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list=userList.stream().map(user -> userService.getsafetyUser(user)).collect(Collectors.toList());
        return ResulUtils.success(list);
    }
    //返回当前登录用户信息
    @GetMapping("/current")
    public BaseResponse <User> getCurrent(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"请先登录");
        }
        long userId = currentUser.getId();
        //校验用户是否合法
        User user = userService.getById(userId);
        User user1 = userService.getsafetyUser(user);
        return ResulUtils.success(user1);
    }

    //主页的分页显示
    //做了一个分功能。每页只显示8条数据
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> RecommendUser(long pageSize , long pageNum, HttpServletRequest request){
        User loginUser  = userService.getLoginUser(request);
        String redisKey = String.format("Rain:user:recommend:%s",loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        //如果有缓存 ，直接读取
        Page<User> userPage =  (Page<User>) valueOperations.get(redisKey);
        if (userPage != null){
            return ResulUtils.success(userPage);
        }

        //无缓存 ，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userList = userService.page(new Page<>(pageNum,pageSize),queryWrapper);
        //写缓存
        try{
            valueOperations.set(redisKey,userList, 40000, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            log.error("redis set key error",e);
        }
        return ResulUtils.success(userList);
    }


    //根据标签查询用户

    @GetMapping("/search/tags")
    public BaseResponse <List<User>> searchUsersByTags(@RequestParam(required = false) List<String>tagNameList){
       if (CollectionUtils.isEmpty(tagNameList)){
           return ResulUtils.error(ErrorCode.PARAMS_ERROR);
       }
       List<User> userList = userService.searchUserByTags(tagNameList);
       return  ResulUtils.success(userList);
    }

//仅管理员可以删除
    @PostMapping ("/delete")
    public BaseResponse<Boolean> deleteUser(long id,HttpServletRequest request){

        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        if (id <=0){
            return null;
        }


        boolean b = userService.removeById(id);
        return ResulUtils.success(b);
    }


    /**
     * 获取用户匹配
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUser(long num, HttpServletRequest request){
        if (num <=0 || num > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User User = userService.getLoginUser(request);
        return ResulUtils.success(userService.matchUser(num,User));
    }



}
