package com.example.shortudy.global.common;

import com.example.shortudy.global.error.ErrorResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 JOST으로 변환 시 포함하지 않음
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorResponse errors;

    // 성공 시 생성자

    public ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
        this.errors = null;
    }
    // 실패 시 생성 자

    public ApiResponse(boolean success, ErrorResponse errors) {
        this.success = success;
        this.data = null;
        this.errors = errors;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ErrorResponse getErrors() {
        return errors;
    }

    // --------------------------------

    // 성공 응답 생성 static 메서드
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    // 데이터 없는 성공 응답
    public static <T> ApiResponse<T> success() {

        return new ApiResponse<>(true, null);
    }

    // 실패 응답 생성 static 메서드
    public static <T> ApiResponse<T> failure(ErrorResponse errors) {
        return new ApiResponse<>(false, errors);
        // == Getter (JSON 변환을 위해 필수) ==
    }
}