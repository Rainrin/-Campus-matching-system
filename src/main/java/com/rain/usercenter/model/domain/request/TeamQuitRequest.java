package com.rain.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户退出请求封装类
 */
@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = 5527000632712441979L;


    /**
     * id
     */
    private Long teamId;




}
