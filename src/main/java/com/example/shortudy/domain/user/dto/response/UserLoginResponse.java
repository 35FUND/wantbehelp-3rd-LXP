package com.example.shortudy.domain.user.dto.response;

public record UserLoginResponse(
        String accessToken,
        String refreshToken
) {
}

