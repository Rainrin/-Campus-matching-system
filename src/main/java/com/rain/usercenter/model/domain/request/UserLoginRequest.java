package com.rain.usercenter.model.domain.request;


import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    //这个是一个序列化接口
    private static final long serialVersionUID = -7332743727878398641L;

    private String userAccount;

    private String userPassword;

}
