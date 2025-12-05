package com.example.shortudy.domain.shorts.dto;

public record ShortsUploadRequest(
    Long userId,
    Long categoryId,
    String title,
    String description,
    String videoUrl,
    String thumbnailUrl,
    Integer durationSec
){
}

