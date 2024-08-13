package com.rain.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 加入队伍请求封装类
 */
@Data
public class TeamJoinRequest implements Serializable {



    private static final long serialVersionUID = -385110337863524469L;
    /**
     * id
     */
    private Long teamId;


    /**
     * 密码
     */
    private String password;






}
