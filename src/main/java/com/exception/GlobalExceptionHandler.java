package com.exception;

import com.common.Result;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ":" + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验异常: {}", message);
        return Result.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ":" + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("绑定异常: {}", message);
        return Result.fail(400, message);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if ("/favicon.ico".equals(requestUri)) {
            log.debug("浏览器图标请求未命中: {}", requestUri);
        } else {
            log.warn("静态资源不存在: method={}, path={}", ex.getHttpMethod(), requestUri);
        }
        return Result.fail(404, "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception ex) {
        log.error("未处理异常", ex);
        return Result.fail(500, ex.getMessage());
    }
}
