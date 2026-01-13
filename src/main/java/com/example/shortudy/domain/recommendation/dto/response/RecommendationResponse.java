package com.example.shortudy.domain.recommendation.dto.response;

import com.example.shortudy.domain.shorts.entity.Shorts;

import java.util.List;

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

    public record ShortsRecommendation(
            Long shortsId,
            String title,
            String thumbnailUrl,
            double similarity
    ) {
        public static ShortsRecommendation from(Shorts shorts, double similarity) {
            return new ShortsRecommendation(
                    shorts.getId(),
                    shorts.getTitle(),
                    shorts.getThumbnailUrl(),
                    Math.round(similarity * 1000.0) / 1000.0
            );
        }
    }

    public record PageInfo(
            int offset,
            int limit,
            int totalCount,
            boolean hasNext,
            Integer nextOffset
    ) {}
}