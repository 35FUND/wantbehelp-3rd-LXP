package com.example.shortudy.global.error.exception;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 * - 통일된 Not Found 예외 처리를 위해 사용
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + "을(를) 찾을 수 없습니다. ID: " + id);
    }
}

