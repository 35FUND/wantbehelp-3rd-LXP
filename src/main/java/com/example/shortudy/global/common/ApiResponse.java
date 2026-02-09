package com.example.shortudy.global.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * API 공통 응답 클래스
 * - success: 요청 성공 여부
 * - message: 응답 메시지
 * - data: 실제 데이터
 */
@Getter
public class ApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private String request;
    private T data;

    // 생성자
    private ApiResponse(boolean success, String code, String message, String request, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.request = request;
        this.data = data;
    }

    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // == 정적 팩토리 메서드 ==

    /**
     * 성공 응답 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(T data) {

        return new ApiResponse<>(true, "Success", null, data);
    }

    /**
     * 성공 응답 (코드/메시지 포함)
     * - 명세가 있는 API에서 사용
     */
    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return new ApiResponse<>(true, code, message, data);
    }

    /**
     * 에러 응답
     * - 기존 시그니처 유지: (message, code, request)
     */
    public static <T> ApiResponse<T> error(String code, String message, String request) {
        return new ApiResponse<>(false, code, message, request, null);
    }
}