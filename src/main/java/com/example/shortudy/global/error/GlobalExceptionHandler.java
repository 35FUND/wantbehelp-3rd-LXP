package com.example.shortudy.global.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BaseException e, HttpServletRequest request) {
        ErrorCode ec = e.errorCode();
        return ResponseEntity.status(ec.status())
                .body(toResponse(ec.status(), ec.code(), e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT.message());

        ErrorCode ec = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(ec.status())
                .body(toResponse(ec.status(), ec.code(), msg, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        ErrorCode ec = ErrorCode.INVALID_INPUT;
        String msg = "요청 파라미터 타입이 올바르지 않습니다.";
        return ResponseEntity.status(ec.status())
                .body(toResponse(ec.status(), ec.code(), msg, request.getRequestURI()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        ErrorCode ec = ErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity.status(ec.status())
                .body(toResponse(ec.status(), ec.code(), ec.message(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception e, HttpServletRequest request) {
        ErrorCode ec = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(ec.status())
                .body(toResponse(ec.status(), ec.code(), ec.message(), request.getRequestURI()));
    }

    private ErrorResponse toResponse(HttpStatus status, String code, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path
        );
    }
}
