package com.example.shortudy.domain.like.dto;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.global.config.S3Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 내가 좋아요한 숏츠 정보 응답 DTO
 * @param shortsId 숏츠 ID
 * @param thumbnailUrl 썸네일 URL
 * @param title 제목
 * @param userNickname 사용자 닉네임
 * @param viewCount 조회수
 * @param createdAt 생성일
 * @param description 설명
 * @param categoryName 카테고리 이름
 * @param keywords 키워드 리스트
 * @param videoUrl 비디오 URL
 * @param commentCount 댓글 수
 * @param likeCount 좋아요 수
 * @param userProfileUrl 사용자 프로필 URL
 * @param durationSec 영상 길이
 */
public record MyLikedShortsResponse(
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
) {
    public static MyLikedShortsResponse from(Shorts shorts, List<String> keywords, Integer commentCount, String userProfileUrl) {
        return new MyLikedShortsResponse(
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
                userProfileUrl,
                shorts.getDurationSec()
        );
    }
}
