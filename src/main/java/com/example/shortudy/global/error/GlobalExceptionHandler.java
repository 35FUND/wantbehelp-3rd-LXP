package com.example.shortudy.global.error;

import com.example.shortudy.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<?>> handleBase(BaseException e, HttpServletRequest request) {
        return error(e.errorCode(), e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT.message());

        return error(ErrorCode.INVALID_INPUT, msg, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {

        return error(ErrorCode.INVALID_INPUT, "요청 파라미터 타입이 올바르지 않습니다.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {

        return error(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.message(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAny(Exception e, HttpServletRequest request) {

        log.error("예기치 못한 오류. path={}", request.getRequestURI(), e);

        return error(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.message(), request);
    }

    private ResponseEntity<ApiResponse<?>> error(ErrorCode ec, String message, HttpServletRequest request) {
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.error(ec.code(), message, request.getRequestURI()));
    }
}
