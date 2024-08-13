package com.rain.usercenter.exception;



import com.rain.usercenter.common.BaseResponse;
import com.rain.usercenter.common.ErrorCode;
import com.rain.usercenter.common.ResulUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 全局异常处理器
 * @author Rain
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        log.error("businessException:"+e.getMessage(),e);
        return ResulUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(BusinessException e){
        log.error("runtimeException:",e);
        return ResulUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }

}
