package com.abc.common;


import com.abc.exception.BusinessException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// 全局异常处理类

//**全局统一异常处理器注解**，搭配 `@ExceptionHandler`，捕获所有 `@RestController` 抛出的异常，
// 统一返回你自定义的 Result JSON，不用每个接口单独 try-catch。
@RestControllerAdvice
public class GlobalExceptionHandler {
    //处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleBindException(MethodArgumentNotValidException e) {
        //异常数据的处理
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(","));
        return Result.error(ResultCode.PARAM_ERROR.getCode(), ResultCode.PARAM_ERROR.getMsg(), message);
    }

    //处理业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        //如果携带数据
        if(e.getData() != null)
            return Result.error(e.getCode(), e.getMessage(), e.getData());
        else
            return Result.error(e.getCode(), e.getMessage(), null);
    }
}
