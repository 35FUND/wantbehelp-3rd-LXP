package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "숏폼 수정 요청")
public record ShortsUpdateRequest (
    @Schema(description = "제목", example = "수정된 제목")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    String title,

    @Schema(description = "설명", example = "수정된 설명")
    String description,

    @Schema(description = "카테고리 ID", example = "2")
    Long categoryId,

    @Schema(description = "썸네일 URL", example = "/uploads/thumbnails/new.jpg")
    String thumbnailUrl,

    @Schema(description = "영상 길이(초)", example = "60")
    @Min(value = 1, message = "영상 길이는 1초 이상이어야 합니다.")
    Integer durationSec,

    @Schema(description = "공개 상태 (PUBLIC, PRIVATE)", example = "PUBLIC")
    ShortsStatus status,

    @Schema(description = "태그 목록", example = "[\"React\", \"프론트엔드\"]")
    List<String> tagNames
){
}
