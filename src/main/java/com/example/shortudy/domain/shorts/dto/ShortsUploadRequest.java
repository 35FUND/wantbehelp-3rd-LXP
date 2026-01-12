package com.example.shortudy.domain.shorts.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ShortsUploadRequest(
    @NotNull(message = "사용자 ID는 필수입니다.")
    Long userId,

    @NotNull(message = "카테고리 ID는 필수입니다.")
    Long categoryId,

    @NotBlank(message = "제목은 필수입니다.")
    String title,

    String description,

    @NotBlank(message = "영상 URL은 필수입니다.")
    String videoUrl,

    String thumbnailUrl,

    @Min(value = 1, message = "영상 길이는 1초 이상이어야 합니다.")
    Integer durationSec,

    List<String> keywordNames
){
}
