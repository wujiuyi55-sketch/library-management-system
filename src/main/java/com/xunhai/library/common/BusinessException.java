package com.xunhai.library.common;

/**
 * 业务异常：可预期、需要把原因返回给调用方的错误（库存不足、重复借阅等）。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
