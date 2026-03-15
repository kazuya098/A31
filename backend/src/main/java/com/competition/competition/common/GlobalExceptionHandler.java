package com.competition.competition.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.stream.Collectors;

/**
 * 全局异常处理：把 Controller/Service 抛出的异常转成统一 JSON 返回。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 生产环境建议改为 "服务器内部错误"，避免把异常详情返回给前端 */
    private static final String GENERIC_ERROR_MESSAGE = "服务器内部错误";// 返回通用的提示

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("unexpected error", e);
        String message = e.getMessage() != null ? e.getMessage() : GENERIC_ERROR_MESSAGE;
        return Result.fail(ResultCode.SERVER_ERROR, message);
    }

    /** @Valid 校验失败：请求体字段不合法（如用户名/密码为空等） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (message.isEmpty()) {
            message = "参数校验失败";
        }
        log.debug("validation failed: {}", message);
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    /** 请求体无法解析：JSON 格式错误、类型不匹配等 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("request body not readable: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, "请求体格式错误");
    }

    /**  数据库相关的异常*/
    /** 数据库主键冲突或唯一约束冲突 */
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(org.springframework.dao.DuplicateKeyException e) {
        log.warn("duplicate key error: {}", e.getMessage());
        return Result.fail(ResultCode.CONFLICT, "数据已存在");
    }

    /** 数据库操作异常（如外键约束、数据完整性问题） */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException e) {
        log.error("data integrity violation: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, "数据操作失败");
    }

    /**文件操作异常*/
    /** 文件保存失败的运行时异常 */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("runtime error: {}", e.getMessage());
        return Result.fail(ResultCode.SERVER_ERROR, e.getMessage());
    }

    /** 文件上传异常（如大小超限、格式问题等） */
    @ExceptionHandler(MultipartException.class)
    public Result<Void> handleMultipart(MultipartException e) {
        log.warn("upload error: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, "文件上传失败");
    }

    /**业务逻辑异常 */
    /** 非法参数异常（业务参数不合法） */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("illegal argument: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, e.getMessage());
    }

    /** 资源未找到异常 */
    @ExceptionHandler(java.util.NoSuchElementException.class)
    public Result<Void> handleNotFound(java.util.NoSuchElementException e) {
        log.warn("resource not found: {}", e.getMessage());
        return Result.fail(ResultCode.NOT_FOUND, "资源不存在");
    }

    /** 算法服务异常 */
    /** 外部服务调用失败 */
    @ExceptionHandler(org.springframework.web.reactive.function.client.WebClientResponseException.class)
    public Result<Void> handleWebClientError(org.springframework.web.reactive.function.client.WebClientResponseException e) {
        log.error("algorithm service error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
        return Result.fail(ResultCode.BAD_GATEWAY, "算法服务调用失败");
    }


}
