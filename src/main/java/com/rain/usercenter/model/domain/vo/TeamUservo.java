package com.rain.usercenter.model.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍和用户信息封装类
 */
@Data
public class TeamUservo implements Serializable {

    private static final long serialVersionUID = -7748786648303002580L;

    /**
     * id
     */
    private Long id;

    /**
     * 队伍昵称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     *  0 - 公开  1- 私有  2- 加密
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 已加入用户数
     */
    private Integer hasJoinNum;
    /**
     * 创建人的用户信息
     *
     */
    UserVo createUser ;

    /**
     * 是否已加入
     */
    private boolean hasJoin = false;
}
