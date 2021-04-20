package com.yuanxiang.gulimall.product.app;

import com.yuanxiang.common.exception.BizCodeException;
import com.yuanxiang.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理所有异常
 */
@Slf4j
//@ControllerAdvice("com.yuanxiang.gulimall.product.app")
@RestControllerAdvice("com.yuanxiang.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value= MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{}:异常类型{}:",e.getMessage(),e.getClass());
        BindingResult bindingResult=e.getBindingResult();
        Map<String, String> map = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        }));
        return R.error(BizCodeException.VALID_EXCEPTION.getCode(),BizCodeException.VALID_EXCEPTION.getMsg()).put("data",map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        log.error("错误:",throwable);
        return R.error(BizCodeException.UNKNOW_EXCEPTION.getCode(),BizCodeException.UNKNOW_EXCEPTION.getMsg());
    }
}
