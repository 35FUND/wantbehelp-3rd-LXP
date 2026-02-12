package com.example.shortudy.domain.recommendation.dto.response;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;

import java.util.List;

/**
 * 추천 숏츠 응답 DTO
 * [구조]
 * RecommendationResponse
 * ├── List<RecommendedShorts>  — 추천 숏츠 목록 (ShortsResponse + similarity)
 * └── PageInfo                 — 오프셋 기반 페이지네이션 정보
 */
public record RecommendationResponse(
        List<RecommendedShorts> recommendations,
        PageInfo pageInfo
) {
    public static RecommendationResponse of(
            List<RecommendedShorts> recommendations,
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
     * [구조]
     * - shorts: ShortsResponse 전체 정보 (재생 화면 구성에 필요한 모든 데이터)
     * - similarity: 자카드 유사도 점수 (추천 알고리즘 메타데이터)
     *
     * @param shorts     숏츠 전체 정보 (ShortsResponse)
     * @param similarity 자카드 유사도 (소수점 3자리)
     */
    public record RecommendedShorts(
            ShortsResponse shorts,
            double similarity
    ) {
        /**
         * ShortsResponse + 유사도 → 추천 DTO 생성
         *
         * @param shortsResponse 숏츠 응답 DTO
         * @param similarity     자카드 유사도 점수
         * @return 추천 아이템 DTO
         */
        public static RecommendedShorts of(ShortsResponse shortsResponse, double similarity) {
            return new RecommendedShorts(
                    shortsResponse,
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