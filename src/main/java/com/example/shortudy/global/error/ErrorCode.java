package com.example.shortudy.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400", "요청 값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "요청 권한이 없습니다."),


    // user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "리소스를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER_401", "유효하지 않은 비밀번호입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_409", "이미 사용 중인 이메일입니다."),
    LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "USER_400", "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "USER_400", "유효하지 않은 토큰정보입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "USER_401", "토큰이 만료되었습니다."),

    // category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_404", "해당 카테고리를 찾을 수 없습니다."),

    // shorts
    SHORTS_NOT_FOUND(HttpStatus.NOT_FOUND, "SHORTS_404", "해당 숏츠를 찾을 수 없습니다."),
    SHORTS_FORBIDDEN(HttpStatus.FORBIDDEN, "SHORTS_403", "해당 숏츠에 대한 접근 권한이 없습니다."),
    SHORTS_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "SHORTS_400", "파일 크기가 허용 범위를 초과했습니다."),
    SHORTS_UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "SHORTS_400", "지원하지 않는 파일 형식입니다."),
    SHORTS_UPLOAD_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SHORTS_UPLOAD_404", "업로드 세션을 찾을 수 없습니다."),
    SHORTS_UPLOAD_OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "SHORTS_UPLOAD_404", "업로드된 파일을 찾을 수 없습니다."),
    SHORTS_UPLOAD_SESSION_EXPIRED(HttpStatus.BAD_REQUEST, "SHORTS_UPLOAD_400", "업로드 세션이 만료되었습니다."),
    AWS_S3_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "S3 설정이 누락되었습니다."),


    // comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404", "해당 댓글을 찾을 수 없습니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT_403", "해당 댓글에 대한 접근 권한이 없습니다."),





    ;
    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}

