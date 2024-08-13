package com.rain.usercenter.model.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

/**
 * 用户信息的包装类（脱敏）
 */
@Data
public class UserVo {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 个人简介
     */
    private String profile;

    /**
     * 性别
     */
    private Integer gender;
    /**
     * 电话
     */
    private String phone;

    /**
     * 标签列表
     */
    private String tags;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0 -- 正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 用户角色 0 - 普通用户  1- 管理员
     */
    private Integer userRole;

    private static final long serialVersionUID = 1L;
}
