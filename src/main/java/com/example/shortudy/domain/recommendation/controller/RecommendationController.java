package com.example.shortudy.domain.recommendation.controller;

import com.example.shortudy.domain.recommendation.dto.request.RecommendationRequest;
import com.example.shortudy.domain.recommendation.dto.response.RecommendationResponse;
import com.example.shortudy.domain.recommendation.service.ShortsRecommendationService;
import com.example.shortudy.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final ShortsRecommendationService recommendationService;

    @GetMapping("/shorts/{shortsId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<RecommendationResponse>> getRecommendations(
            @PathVariable String shortsId,
            @Valid @ModelAttribute RecommendationRequest request
    ) {
        List<RecommendationResponse> recommendations =
                recommendationService.getRecommendations(shortsId, request.limit());

        return ApiResponse.success(recommendations);
    }
}