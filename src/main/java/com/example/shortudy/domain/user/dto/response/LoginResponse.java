package com.example.shortudy.domain.user.dto.response;

public record LoginResponse(
        String AccessToken,
        String RefreshToken
) {
}

