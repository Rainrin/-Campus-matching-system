package com.rain.usercenter.model.domain.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamRequest implements Serializable {
    private static final long serialVersionUID = 5527000632712441979L;


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
     * 密码
     */
    private String password;

    /**
     *  0 - 公开  1- 私有  2- 加密
     */
    private Integer status;





}
