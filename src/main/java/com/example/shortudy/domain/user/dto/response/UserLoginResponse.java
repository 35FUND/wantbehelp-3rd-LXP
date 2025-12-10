package com.example.shortudy.domain.user.dto.response;

import com.example.shortudy.domain.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record UserLoginResponse(
        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIs...")
        String accessToken,

        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIs...")
        String refreshToken,

        @Schema(description = "사용자 정보")
        UserResponse user
) {
}

