package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ShortsUpdateRequest (
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    String title,
    String description,
    Long categoryId,
    String thumbnailUrl,
    @Min(value = 1, message = "영상 길이는 1초 이상이어야 합니다.")
    Integer durationSec,
    ShortsStatus status,
    List<String> keywords
){
}
