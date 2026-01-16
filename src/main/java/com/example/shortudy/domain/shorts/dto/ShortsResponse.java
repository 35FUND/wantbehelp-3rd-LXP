package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;


public record ShortsResponse(
        Long shortsId,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        Integer duration,           // durationSec → duration으로 변경
        ShortsStatus status,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        CategoryDto category,       // nested 구조 유지
        List<String> keywords,
        UploaderDto uploader,       // 🆕 이름 변경 (user → uploader)

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        LocalDateTime updatedAt
) {

    /**
     * Uploader 정보 (nested object)
     */
    public record UploaderDto(
            Long userId,
            String nickname,
            String profileImageUrl
    ) {}

    /**
     * Category 정보 (nested object)
     */
    public record CategoryDto(
            Long id,
            String name
    ) {}

    /**
     * Entity → DTO 변환
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
                shorts.getViewCount() != null ? shorts.getViewCount().intValue() : 0,  // Long → Integer 변환
                shorts.getLikeCount() != null ? shorts.getLikeCount() : 0,
                0,  // commentCount는 별도 계산 필요 (TODO)
                new CategoryDto(
                        shorts.getCategory().getId(),
                        shorts.getCategory().getName()
                ),
                shorts.getKeywordNames(),
                new UploaderDto(
                        shorts.getUser().getId(),
                        shorts.getUser().getNickname(),
                        shorts.getUser().getProfileUrl()
                ),
                shorts.getCreatedAt(),
                shorts.getUpdatedAt()
        );
    }
}



