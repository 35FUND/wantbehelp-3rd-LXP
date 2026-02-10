package com.example.shortudy.domain.like.dto;

import com.example.shortudy.domain.like.entity.ShortsLike;

/**
 * 숏츠 좋아요 응답 DTO
 */
public record ShortsLikeResponse(
        Long shortsId,
        Long userId,
        boolean isLiked
) {
    public static ShortsLikeResponse from(Long shortId, Long userId, boolean isLiked) {
        return new ShortsLikeResponse(
                shortId,
                userId,
                isLiked
        );
    }
}
