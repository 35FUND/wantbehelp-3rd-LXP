package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;

import java.time.LocalDateTime;

public record ShortsResponse (
       Long shortsId,
       String uploaderNickname,
       String categoryName,
       String title,
       String description,
       String videoUrl,
       String thumbnailUrl,
       ShortsStatus status,
       LocalDateTime createdAt

){
    //정적 팩토리 메서드 Entity -> DTO 변환
    public static ShortsResponse from(Shorts shorts){
        return new ShortsResponse(
              shorts.getId(),
                shorts.getUser().getNickname(),
                shorts.getCategory().getName(),
                shorts.getTitle(),
                shorts.getTitle(),
                shorts.getVideoUrl(),
                shorts.getThumbnailUrl(),
                shorts.getShortsStatus(),
                shorts.getCreatedAt()

        );
    }
}




