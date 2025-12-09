package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 숏폼 응답 DTO
 * - 민감 정보(이메일, 비밀번호 등) 제외
 * - 프론트에서 필요한 정보만 포함
 */
@Schema(description = "숏폼 응답")
public record ShortsResponse (
       @Schema(description = "숏폼 ID", example = "1")
       Long shortsId,

       @Schema(description = "업로더 ID", example = "1")
       Long uploaderId,

       @Schema(description = "업로더 닉네임", example = "홍길동")
       String uploaderNickname,

       @Schema(description = "카테고리 ID", example = "1")
       Long categoryId,

       @Schema(description = "카테고리 이름", example = "프로그래밍")
       String categoryName,

       @Schema(description = "제목", example = "Spring Boot 시작하기")
       String title,

       @Schema(description = "설명", example = "Spring Boot 입문 가이드")
       String description,

       @Schema(description = "영상 URL", example = "/uploads/videos/abc123.mp4")
       String videoUrl,

       @Schema(description = "썸네일 URL", example = "/uploads/thumbnails/abc123.jpg")
       String thumbnailUrl,

       @Schema(description = "영상 길이(초)", example = "58")
       Integer durationSec,

       @Schema(description = "공개 상태", example = "PUBLIC")
       ShortsStatus status,

       @Schema(description = "생성일시", example = "2025-01-09T10:30:00")
       LocalDateTime createdAt,

       @Schema(description = "태그 목록", example = "[\"Java\", \"Spring\"]")
       List<String> tagNames
){
    /**
     * Entity -> DTO 변환
     * User의 민감 정보(email, password)는 제외하고 닉네임만 반환
     */
    public static ShortsResponse from(Shorts shorts){
        List<String> tags = shorts.getTaggings().stream()
                .map(tagging -> tagging.getTag().getDisplayName())
                .toList();

        return new ShortsResponse(
                shorts.getId(),
                shorts.getUser().getId(),           // 업로더 ID만
                shorts.getUser().getNickname() != null
                    ? shorts.getUser().getNickname()
                    : shorts.getUser().getName(),   // 닉네임 없으면 이름
                shorts.getCategory().getId(),
                shorts.getCategory().getName(),
                shorts.getTitle(),
                shorts.getDescription(),
                shorts.getVideoUrl(),
                shorts.getThumbnailUrl(),
                shorts.getDurationSec(),
                shorts.getShortsStatus(),
                shorts.getCreatedAt(),
                tags
        );
    }
}




