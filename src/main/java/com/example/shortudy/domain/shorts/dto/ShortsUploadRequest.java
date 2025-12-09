package com.example.shortudy.domain.shorts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "숏폼 업로드 요청")
public record ShortsUploadRequest(
    @Schema(description = "업로더 ID", example = "1")
    @NotNull(message = "사용자 ID는 필수입니다.")
    Long userId,

    @Schema(description = "카테고리 ID", example = "1")
    @NotNull(message = "카테고리 ID는 필수입니다.")
    Long categoryId,

    @Schema(description = "영상 제목", example = "Spring Boot 시작하기")
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @Schema(description = "영상 설명", example = "Spring Boot 입문자를 위한 가이드입니다.")
    String description,

    @Schema(description = "영상 URL", example = "/uploads/videos/abc123.mp4")
    @NotBlank(message = "영상 URL은 필수입니다.")
    String videoUrl,

    @Schema(description = "썸네일 URL", example = "/uploads/thumbnails/abc123.jpg")
    String thumbnailUrl,

    @Schema(description = "영상 길이(초)", example = "58")
    @Min(value = 1, message = "영상 길이는 1초 이상이어야 합니다.")
    Integer durationSec,

    @Schema(description = "태그 목록", example = "[\"Java\", \"Spring\", \"백엔드\"]")
    List<String> tagNames
){
}
