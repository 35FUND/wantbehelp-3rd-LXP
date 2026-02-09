package com.example.shortudy.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400", "요청 값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "요청 권한이 없습니다."),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "COMMON_501", "아직 구현되지 않았습니다."),


    // user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "리소스를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_400", "비밀번호가 다릅니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_409", "이미 사용 중인 이메일입니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "USER_401", "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "USER_401", "유효하지 않은 토큰정보입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "USER_401", "토큰이 만료되었습니다."),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "USER_401", "변경할 비밀번호가 현재 비밀번호와 같습니다."),
    UnsupportedImageFormatException(HttpStatus.BAD_REQUEST, "USER_400", "유저 프로필 이미지에 적합하지 않은 파일 유형입니다."),
    AccessDeniedException(HttpStatus.FORBIDDEN, "USER_403", "본인의 프로필 이미지만 설정할 수 있습니다."),
    UserDeleteNotAllowedException(HttpStatus.CONFLICT, "USER_409", "회원 탈퇴가 불가능한 회원입니다."),
    EmailBlankException(HttpStatus.BAD_REQUEST, "USER_400", "이메일은 빈 값을 입력할 수 없습니다."),


    // category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_404", "해당 카테고리를 찾을 수 없습니다."),
    DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "CATEGORY_409", "이미 존재하는 카테고리 이름입니다."),

    // shorts
    SHORTS_NOT_FOUND(HttpStatus.NOT_FOUND, "SHORTS_404", "해당 숏츠를 찾을 수 없습니다."),
    SHORTS_UPLOADER_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "SHORTS_500", "숏츠의 작성자 정보를 찾을 수 없습니다."),
    SHORTS_CATEGORY_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "SHORTS_500", "숏츠의 카테고리 정보를 찾을 수 없습니다."),
    SHORTS_TITLE_INVALID(HttpStatus.BAD_REQUEST, "SHORTS_400", "숏츠 제목은 필수이며 100자를 초과할 수 없습니다."),
    SHORTS_URL_INVALID(HttpStatus.BAD_REQUEST, "SHORTS_400", "URL 형식이 올바르지 않거나 길이 제한을 초과했습니다."),
    SHORTS_ESSENTIAL_INFO_MISSING(HttpStatus.BAD_REQUEST, "SHORTS_400", "작성자(User) 및 카테고리(Category) 정보는 필수입니다."),
    SHORTS_DURATION_INVALID(HttpStatus.BAD_REQUEST, "SHORTS_407", "영상 길이는 1초 이상이어야 합니다."),
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

    // Keyword
    KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "KEYWORD_404", "해당 키워드를 찾을 수 없습니다."),
    DUPLICATE_KEYWORD(HttpStatus.CONFLICT, "KEYWORD_409", "이미 등록된 키워드입니다."),
    SPACE_ONLY_KEYWORD(HttpStatus.BAD_REQUEST, "KEYWORD_400", "공백만으로 이루어진 키워드는 허용되지 않습니다."),



    // playlist
    PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAYLIST_404", "해당 플레이리스트를 찾을 수 없습니다."),
    PLAYLIST_FORBIDDEN(HttpStatus.FORBIDDEN, "PLAYLIST_403", "해당 플레이리스트에 대한 접근 권한이 없습니다."),
    ALREADY_ADDED_SHORTS(HttpStatus.BAD_REQUEST, "PLAYLIST_409", "이미 플레이리스트에 추가된 숏츠입니다."),
    PLAYLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAYLIST_404", "이미 플레이리스트에서 삭제된 숏츠입니다."),

    // shortsLike
    ALREADY_UNLIKE(HttpStatus.BAD_REQUEST, "LIKE_400", "좋아요하지 않았습니다."),
    ALREADY_LIKE(HttpStatus.BAD_REQUEST, "LIKE_400", "이미 좋아요했습니다."),

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

