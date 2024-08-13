package com.rain.usercenter.model.domain.request;


import lombok.Data;

import java.io.Serializable;

/**
 *
 * 用户注册请求体
 *
 */


@Data
public class UserRegisterRequest implements Serializable {

    //这个是一个序列化接口
    private static final long serialVersionUID = -7332743727878398641L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;
}
