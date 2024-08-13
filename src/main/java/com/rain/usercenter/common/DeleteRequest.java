package com.rain.usercenter.common;


import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求参数
 */
@Data
public class DeleteRequest implements Serializable {


    private static final long serialVersionUID = 5612503019060308582L;

    private  long id ;
}
