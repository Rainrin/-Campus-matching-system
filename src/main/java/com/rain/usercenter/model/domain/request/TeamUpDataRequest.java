package com.rain.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamUpDataRequest implements Serializable {

    private static final long serialVersionUID = 7803393256375321714L;
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
     * 过期时间
     */
    private Date expireTime;

    /**
     * 密码
     */
    private String password;

    /**
     *  0 - 公开  1- 私有  2- 加密
     */
    private Integer status;


}
