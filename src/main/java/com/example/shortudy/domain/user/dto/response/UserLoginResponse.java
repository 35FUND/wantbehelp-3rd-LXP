package com.example.shortudy.domain.user.dto.response;

import com.example.shortudy.domain.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 (토큰은 쿠키로 전달)")
public record UserLoginResponse(

        @Schema(description = "사용자 정보")
        UserResponse user
) {
}

