package com.example.shortudy.domain.comment.dto.response;

import com.example.shortudy.domain.user.entity.User;

public record WriterResponse(
        Long userId,
        String nickname,
        String profileImageUrl
) {
    public static WriterResponse from(User user) {

        return new WriterResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileUrl()
        );
    }
}
