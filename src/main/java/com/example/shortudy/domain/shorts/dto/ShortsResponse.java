package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.tagging.Tagging;

import java.time.LocalDateTime;
import java.util.List;

public record ShortsResponse (
       Long shortsId,
       String uploaderNickname,
       String categoryName,
       String title,
       String description,
       String videoUrl,
       String thumbnailUrl,
       Integer durationSec,
       ShortsStatus status,
       LocalDateTime createdAt,
       List<String> tagNames
){
    //정적 팩토리 메서드 Entity -> DTO 변환
    public static ShortsResponse from(Shorts shorts){
        List<String> tags = shorts.getTaggings().stream()
                .map(tagging -> tagging.getTag().getDisplayName())
                .toList();

        return new ShortsResponse(
              shorts.getId(),
                shorts.getUser().getNickname(),
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




