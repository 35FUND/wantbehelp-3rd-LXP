package com.example.shortudy.domain.user.dto.response;

public record InfoResponse(
        Long userId,
        String email,
        String nickName,
        String profileUrl
) {
}
