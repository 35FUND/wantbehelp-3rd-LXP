package com.example.shortudy.domain.like.dto;

/**
 * 좋아요 토글 결과 DTO
 * @param isLiked 좋아요 등록됨/취소됨 여부
 * @param likeCount 숏츠의 총 좋아요 수
 */
public record LikeToggleResponse(
   boolean isLiked,
   int likeCount
) {
}
