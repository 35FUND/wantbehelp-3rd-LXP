package com.example.shortudy.domain.recommendation.dto.response;

import com.example.shortudy.domain.shorts.entity.Shorts;

public record RecommendationResponse(
    Long shortsId,
    String title,
    String thumbnailUrl,
    double similarity
) {
    public static RecommendationResponse from(Shorts shorts, double similarity) {
        return new RecommendationResponse(
            shorts.getId(),
            shorts.getTitle(),
            shorts.getThumbnailUrl(),
            Math.round(similarity * 1000.0) / 1000.0
        );
    }
}
