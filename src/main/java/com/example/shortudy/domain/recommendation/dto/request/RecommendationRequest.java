package com.example.shortudy.domain.recommendation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RecommendationRequest(

        @Min(value = 0, message = "offset은 0 이상이어야 합니다.")
        Integer offset,

        @Min(value = 1, message = "최소 1개 이상 요청해야 합니다.")
        @Max(value = 20, message = "최대 20개까지 요청 가능합니다.")
        Integer limit
) {
    public RecommendationRequest {
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = 20;
        }
    }
}