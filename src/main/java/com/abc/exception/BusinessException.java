package com.abc.exception;

import lombok.Getter;

/**
 * 业务异常类
 * @author system
 */
@Getter // 生成 getter 方法
public class BusinessException extends RuntimeException {

    private final String code;
    private final String message;
    private final Object data;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
        this.message = message;
        this.data = null;
    }
}
