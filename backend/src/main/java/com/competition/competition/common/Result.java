package com.competition.competition.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 返回格式（前后端约定好的结构）。
 * 所有接口返回 { code, message, data }，便于前端统一处理。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 业务状态码，与 HTTP 状态码对齐（200 成功、4xx 客户端错误、5xx 服务端错误） */
    private int code;
    /** 提示信息 */
    private String message;
    /** 业务数据，失败时通常为 null */
    private T data;

    /** 成功并返回数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.OK.getCode(), ResultCode.OK.getDefaultMessage(), data);
    }

    /** 成功且无数据（如登出、修改密码等） */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /** 失败：指定状态码和提示 */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 失败：使用枚举的默认提示 */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getDefaultMessage(), null);
    }

    /** 失败：使用枚举的状态码，自定义提示（覆盖默认） */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    /** 失败：仅提示信息，默认 500（用于未知异常等） */
    public static <T> Result<T> fail(String message) {
        return fail(ResultCode.SERVER_ERROR, message);
    }
}
