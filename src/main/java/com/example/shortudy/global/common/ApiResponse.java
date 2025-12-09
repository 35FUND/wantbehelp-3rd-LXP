package com.example.shortudy.global.common;

/**
 * API 공통 응답 클래스
 * - success: 요청 성공 여부
 * - message: 응답 메시지
 * - data: 실제 데이터
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // 생성자
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // == 정적 팩토리 메서드 ==

    /**
     * 성공 응답 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, "Success", null);
    }

    /**
     * 에러 응답
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // == Getter ==
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}

