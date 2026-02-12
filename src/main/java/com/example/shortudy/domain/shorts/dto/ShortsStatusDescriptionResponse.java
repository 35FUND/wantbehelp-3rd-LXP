package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.shorts.entity.ShortsVisibility;
import java.time.LocalDateTime;
import java.util.List;

public record ShortsStatusDescriptionResponse(
    Long shortsId,
    String title,
    String description,
    String videoUrl,
    String thumbnailUrl,
    Integer durationSec,
    ShortsStatus status,
    String shortsStatusDescription,
    ShortsVisibility visibility,
    Long userId,
    String userNickname,
    String userProfileUrl,
    Long categoryId,
    String categoryName,
    List<String> keywords,
    Long viewCount,
    Integer likeCount,
    Long commentCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Boolean isLiked
) {
    public static ShortsStatusDescriptionResponse of(ShortsResponse response, String shortsStatusDescription) {
        return new ShortsStatusDescriptionResponse(
            response.shortsId(),
            response.title(),
            response.description(),
            response.videoUrl(),
            response.thumbnailUrl(),
            response.durationSec(),
            response.status(),
            shortsStatusDescription,
            response.visibility(),
            response.userId(),
            response.userNickname(),
            response.userProfileUrl(),
            response.categoryId(),
            response.categoryName(),
            response.keywords(),
            response.viewCount(),
            response.likeCount(),
            response.commentCount(),
            response.createdAt(),
            response.updatedAt(),
            response.isLiked()
        );
    }
}
