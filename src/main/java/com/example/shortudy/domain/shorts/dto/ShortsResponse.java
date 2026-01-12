package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;

public record ShortsResponse (

       String shortsId,
       String title,
       String description,
       String videoUrl,
       String thumbnailUrl,
       Integer durationSec,
       ShortsStatus status,
       UploaderDto uploader,
       CategoryDto category
) {
    public record UploaderDto(
            Long userId,
            String nickname,
            String profileUrl
    ) {}

    public record CategoryDto(
            Long categoryId,
            String name
    ) {}

    public static ShortsResponse from(Shorts shorts) {
        return new ShortsResponse(
                shorts.getId().toString(),
                shorts.getTitle(),
                shorts.getDescription(),
                shorts.getVideoUrl(),
                shorts.getThumbnailUrl(),
                shorts.getDurationSec(),
                shorts.getStatus(),
                new UploaderDto(
                        shorts.getUser().getId(),
                        shorts.getUser().getNickname(),
                        shorts.getUser().getProfileUrl()
                ),
                new CategoryDto(
                        shorts.getCategory().getId(),
                        shorts.getCategory().getName()
                )
        );
    }
}




