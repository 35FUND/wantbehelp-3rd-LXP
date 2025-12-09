package com.example.shortudy.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 상태 응답")
public record AuthStatusResponse(
        @Schema(description = "로그인 여부", example = "true")
        boolean isLoggedIn,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "닉네임", example = "길동이")
        String nickname
) {
    public static AuthStatusResponse loggedIn(String email, String name, String nickname) {
        return new AuthStatusResponse(true, email, name, nickname);
    }

    public static AuthStatusResponse notLoggedIn() {
        return new AuthStatusResponse(false, null, null, null);
    }
}

