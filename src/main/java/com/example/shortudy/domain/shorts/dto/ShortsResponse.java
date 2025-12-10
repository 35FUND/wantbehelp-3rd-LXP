package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.Shorts;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 숏폼 응답 DTO (MVP 명세)
 * - 필수 필드만 포함
 * - Uploader와 Category는 nested 객체로 반환
 */
@Schema(description = "숏폼 응답")
public record ShortsResponse (
       @Schema(description = "숏폼 ID", example = "12")
       Long shortsId,

       @Schema(description = "제목", example = "스프링 DI 요약")
       String title,

       @Schema(description = "설명", example = "1분 설명")
       String description,

       @Schema(description = "영상 URL", example = "https://cdn.xxx/v.mp4")
       String videoUrl,

       @Schema(description = "썸네일 URL", example = "https://cdn.xxx/t.png")
       String thumbnailUrl,

       @Schema(description = "업로더 정보")
       UploaderDto uploader,

       @Schema(description = "카테고리 정보")
       CategoryDto category

       // 🚫 MVP 명세 제외 필드 (주석 처리)
       // Long uploaderId,
       // String uploaderNickname,
       // Long categoryId,
       // String categoryName,
       // Integer durationSec,
       // ShortsStatus status,
       // LocalDateTime createdAt,
       // List<String> tagNames
) {
    /**
     * Uploader 정보 (nested object)
     */
    public record UploaderDto(
            @Schema(description = "업로더 ID", example = "3")
            Long userId,

            @Schema(description = "업로더 닉네임", example = "코딩맨")
            String nickname,

            @Schema(description = "프로필 URL", example = "https://cdn.xxx/p.png")
            String profileUrl
    ) {}

    /**
     * Category 정보 (nested object)
     */
    public record CategoryDto(
            @Schema(description = "카테고리 ID", example = "1")
            Long categoryId,

            @Schema(description = "카테고리 이름", example = "프로그래밍")
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
                new UploaderDto(
                        shorts.getUser().getId(),
                        shorts.getUser().getNickname() != null
                                ? shorts.getUser().getNickname()
                                : shorts.getUser().getName(),
                        shorts.getUser().getProfileUrl()
                ),
                new CategoryDto(
                        shorts.getCategory().getId(),
                        shorts.getCategory().getName()
                )
        );
    }
}




