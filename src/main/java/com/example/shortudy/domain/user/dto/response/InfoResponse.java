package com.example.shortudy.domain.user.dto.response;

import com.example.shortudy.domain.user.entity.User;

public record InfoResponse(
        Long userId,
        String email,
        String nickName,
        String profileUrl
) {

    public static InfoResponse from(User user) {
        return new InfoResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileUrl());
    }
}
