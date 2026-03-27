package com.competition.competition.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

/**
 * 全局异常处理：把 Controller/Service 抛出的异常转成统一 JSON 返回。
 * 【需填充】：按业务补充更多 @ExceptionHandler（如业务自定义异常、校验异常等）。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("unexpected error", e);
        return Result.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public Result<Void> handleMultipart(MultipartException e) {
        log.warn("upload error: {}", e.getMessage());
        return Result.fail(HttpStatus.BAD_REQUEST.value(), "文件上传失败");
    }
}
