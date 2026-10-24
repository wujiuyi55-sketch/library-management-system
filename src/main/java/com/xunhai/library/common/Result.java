package com.xunhai.library.common;

import lombok.Data;

/**
 * 统一响应体。
 *
 * <p>code = 0 表示成功，非 0 表示业务或系统错误；HTTP 状态码同时保留语义。</p>
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.message = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }
}
