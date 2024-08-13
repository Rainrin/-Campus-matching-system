package com.rain.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rain.usercenter.common.BaseResponse;
import com.rain.usercenter.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * * 用户服务
 *
 * @author Rain
 */
public interface UserService extends IService<User> {



    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户  id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword  用户密码
     * @return      返回脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销
     */
    int userLoginOut(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUserByTags(List<String> tagNameList);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getsafetyUser(User originUser);

    /**
     * 更新用户信息
     * @param user
     * @param loginUser
     * @return
     */
    int updateUser(User user ,User loginUser);

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 是否为管理员
     * @param request
     * @return
     */
     boolean isAdmin(HttpServletRequest request);


    boolean isAdmin(User loginUser);


    /**
     * 用户匹配
     * @param num
     * @param user
     * @return
     */
    List<User> matchUser(long num, User user);





}
