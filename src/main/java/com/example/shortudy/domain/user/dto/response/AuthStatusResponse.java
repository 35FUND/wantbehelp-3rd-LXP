package com.example.shortudy.domain.user.dto.response;

/**
 * 로그인 상태 응답 DTO
 */
public record AuthStatusResponse(
        boolean isLoggedIn,
        String email,
        String name,
        String nickname
) {
    public static AuthStatusResponse loggedIn(String email, String name, String nickname) {
        return new AuthStatusResponse(true, email, name, nickname);
    }

    public static AuthStatusResponse notLoggedIn() {
        return new AuthStatusResponse(false, null, null, null);
    }
}

