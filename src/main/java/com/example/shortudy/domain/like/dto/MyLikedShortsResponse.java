package com.example.shortudy.domain.like.dto;

import com.example.shortudy.domain.shorts.entity.Shorts;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public record MyLikedShortsResponse(
    List<MyLikedShorts> content,
    Pageable pageable

) {
    public static MyLikedShortsResponse from(List<MyLikedShorts> shorts, Pageable pageable) {
        return new MyLikedShortsResponse(shorts, pageable);
    }

    public record MyLikedShorts (
        Long shortsId,
        String thumbnailUrl,
        String title,
        String userNickname,
        Long viewCount,
        LocalDateTime createdAt,
        String description,
        String categoryName,
        List<String> keywords,
        String videoUrl,
        Integer commentCount,
        Integer likeCount,
        String userProfileUrl,
        Integer durationSec
    ){
        public static MyLikedShorts from(Shorts shorts, int commentCount, List<String> keywords) {
            return new MyLikedShorts(
                    shorts.getId(),
                    shorts.getThumbnailUrl(),
                    shorts.getTitle(),
                    shorts.getUser().getNickname(),
                    shorts.getViewCount(),
                    shorts.getCreatedAt(),
                    shorts.getDescription(),
                    shorts.getCategory().getName(),
                    keywords,
                    shorts.getVideoUrl(),
                    commentCount,
                    shorts.getLikeCount(),
                    shorts.getUser().getProfileUrl(),
                    shorts.getDurationSec()
            );
        }
    }

}
