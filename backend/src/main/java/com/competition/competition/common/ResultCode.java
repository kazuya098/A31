package com.competition.competition.common;

import lombok.Getter;

/**
 * 业务状态码枚举，与前端约定一致，避免魔法数字。
 * 与 HTTP 状态码对齐，便于理解和调试。
 */
@Getter
public enum ResultCode {

    OK(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    SERVER_ERROR(500, "服务器内部错误"),
    BAD_GATEWAY(502, "上游服务异常");

    private final int code;
    private final String defaultMessage;

    ResultCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
