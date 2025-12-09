package com.example.shortudy.domain.user.dto;

import com.example.shortudy.domain.user.entity.User;

public record UserResponse(
        Long id,
        String email,
        String name,
        String nickname
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname()
        );
    }
}

