package com.hmdp.config;

import com.hmdp.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        Object value = e.getValue();
        String rawValue = value == null ? "" : value.toString();
        if ("undefined".equalsIgnoreCase(rawValue) || "null".equalsIgnoreCase(rawValue) || rawValue.isBlank()) {
            log.warn("参数为空或非法：{}={}", e.getName(), rawValue);
            return Result.fail("参数错误");
        }
        log.error("参数类型转换失败：{}={}", e.getName(), rawValue, e);
        return Result.fail("参数格式错误");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return Result.fail("服务器异常");
    }
}
