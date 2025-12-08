package com.example.shortudy.domain.shorts.dto;

public record ShortsUpdateRequest (
    String title,
    String description,
    Long categoryId,
    String thumbnailUrl
){
}
