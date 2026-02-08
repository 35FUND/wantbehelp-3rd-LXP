package com.example.shortudy.domain.recommendation.dto.response;

import com.example.shortudy.domain.shorts.entity.Shorts;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 추천 숏츠 응답 DTO
 * [구조]
 * RecommendationResponse
 * ├── List<ShortsRecommendation>  — 추천 숏츠 목록 (프론트 카드 UI용)
 * └── PageInfo                    — 오프셋 기반 페이지네이션 정보
 */
public record RecommendationResponse(
        List<ShortsRecommendation> recommendations,
        PageInfo pageInfo
) {
    public static RecommendationResponse of(
            List<ShortsRecommendation> recommendations,
            int offset,
            int limit,
            int totalCount
    ) {
        boolean hasNext = (offset + limit) < totalCount;
        Integer nextOffset = hasNext ? offset + limit : null;

        return new RecommendationResponse(
                recommendations,
                new PageInfo(offset, limit, totalCount, hasNext, nextOffset)
        );
    }

    /**
     * 추천 숏츠 개별 항목
     * [필드 네이밍 규칙 — Shorts 도메인 ShortsResponse와의 구분]
     * - ShortsResponse: userId, userNickname, userProfileUrl (플랫 구조)
     * - 여기서는: uploaderNickname (추천 카드에서 "누가 올린 건지"만 표시)
     * - videoUrl, commentCount, isLiked 등 추천 카드에 불필요한 정보는 제외
     *
     * @param shortsId           숏츠 ID
     * @param title              숏츠 제목
     * @param thumbnailUrl       썸네일 URL
     * @param uploaderNickname   업로더 닉네임 (ShortsResponse의 userNickname과 구분)
     * @param categoryName       카테고리명
     * @param viewCount          조회수
     * @param likeCount          좋아요 수
     * @param keywords           키워드 displayName 목록
     * @param durationSec        영상 길이 (초)
     * @param similarity         자카드 유사도 (소수점 3자리)
     */
    public record ShortsRecommendation(
            Long shortsId,
            String title,
            String thumbnailUrl,
            String uploaderNickname,
            String categoryName,
            Long viewCount,
            Integer likeCount,
            List<String> keywords,
            Integer durationSec,
            double similarity
    ) {
        /**
         * Shorts 엔티티 → 추천 DTO 변환
         * - fetch join으로 user, category, shortsKeywords가 이미 로딩된 상태 전제
         *
         * @param recommendedShorts 추천 대상 숏츠 엔티티
         * @param similarity        자카드 유사도 점수
         * @return 추천 카드 DTO
         */
        public static ShortsRecommendation from(Shorts recommendedShorts, double similarity) {
            List<String> keywordNames = recommendedShorts.getShortsKeywords().stream()
                    .map(sk -> sk.getKeyword().getDisplayName())
                    .collect(Collectors.toList());

            return new ShortsRecommendation(
                    recommendedShorts.getId(),
                    recommendedShorts.getTitle(),
                    recommendedShorts.getThumbnailUrl(),
                    recommendedShorts.getUser().getNickname(),
                    recommendedShorts.getCategory().getName(),
                    recommendedShorts.getViewCount(),
                    recommendedShorts.getLikeCount(),
                    keywordNames,
                    recommendedShorts.getDurationSec(),
                    Math.round(similarity * 1000.0) / 1000.0
            );
        }
    }

    /**
     * 오프셋 기반 페이지네이션 정보
     *
     * @param offset     현재 오프셋
     * @param limit      페이지 크기
     * @param totalCount 전체 결과 수 (유사도 > 0인 것만)
     * @param hasNext    다음 페이지 존재 여부
     * @param nextOffset 다음 페이지 오프셋 (없으면 null)
     */
    public record PageInfo(
            int offset,
            int limit,
            int totalCount,
            boolean hasNext,
            Integer nextOffset
    ) {}
}