package com.example.shortudy.global.common;

public class ApiResponse<T> {

    private String message; // 응답 메시지 (예: "Success", "Fail")
    private T data;         // 실제 데이터 (Generic으로 유연하게 받음)

    // 생성자 (private으로 막고 정적 팩토리 메서드 사용 권장)
    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    // == 정적 팩토리 메서드 (생성 편의성) ==

    // 성공 시 데이터와 함께 반환
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("Success", data);
    }

    // 성공이지만 반환할 데이터가 없을 때 (예: 삭제 성공)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("Success", null);
    }

    // == Getter (JSON 변환을 위해 필수) ==
    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}

