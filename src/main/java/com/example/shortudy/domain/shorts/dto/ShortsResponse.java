package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;

/**
 * 숏폼 응답 DTO (MVP 명세)
 * - 필수 필드만 포함
 * - Uploader와 Category는 nested 객체로 반환
 */
public record ShortsResponse (

       Long shortsId,
       String title,
       String description,
       String videoUrl,
       String thumbnailUrl,
       Integer durationSec,
       ShortsStatus status,
       UploaderDto uploader,
       CategoryDto category

       // 🚫 MVP 명세 제외 필드 (주석 처리)
       // Long uploaderId,
       // String uploaderNickname,
       // Long categoryId,
       // String categoryName,
       // LocalDateTime createdAt,
       // List<String> tagNames
) {
    /**
     * Uploader 정보 (nested object)
     */
    public record UploaderDto(
            Long userId,
            String nickname,
            String profileUrl
    ) {}

    /**
     * Category 정보 (nested object)
     */
    public record CategoryDto(
            Long categoryId,
            String name
    ) {}

    /**
     * Entity -> DTO 변환
     */
    public static ShortsResponse from(Shorts shorts) {
        return new ShortsResponse(
                shorts.getId(),
                shorts.getTitle(),
                shorts.getDescription(),
                shorts.getVideoUrl(),
                shorts.getThumbnailUrl(),
                shorts.getDurationSec(),
                shorts.getStatus(),
                new UploaderDto(
                        shorts.getUser().getId(),
                        shorts.getUser().getNickname(),
                        shorts.getUser().getProfileUrl()
                ),
                new CategoryDto(
                        shorts.getCategory().getId(),
                        shorts.getCategory().getName()
                )
        );
    }
}




