package com.example.shortudy.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 재발급 요청 DTO
 */
public record TokenRefreshRequest(
        @NotBlank(message = "Refresh Token은 필수입니다.")
        String refreshToken
) {
}

