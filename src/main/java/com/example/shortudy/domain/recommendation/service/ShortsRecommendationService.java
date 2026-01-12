package com.example.shortudy.domain.recommendation.service;

import com.example.shortudy.domain.recommendation.dto.response.RecommendationResponse;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.shorts.entity.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShortsRecommendationService {

    private final ShortsRepository shortsRepository;

    public List<RecommendationResponse> getRecommendations(String shortsId, int limit) {
        Long shortsIdLong = Long.parseLong(shortsId);

        Shorts baseShorts = shortsRepository.findById(shortsIdLong)
                .orElseThrow(() -> new IllegalArgumentException("쇼츠를 찾을 수 없습니다."));
        Set<String> baseKeywords = extractKeywords(baseShorts);

        // 주의: ShortsRepository 인터페이스에 findByIdNot 메소드가 선언되어 있어야 합니다.
        List<Shorts> candidates = shortsRepository.findByIdNot(shortsIdLong);

        Map<String, Shorts> candidateMap = candidates.stream()
                .collect(Collectors.toMap(
                        s -> s.getId().toString(),
                        Function.identity()
                ));

        List<JaccardSimilarityCalculator.SimilarityInput> similarityInputs = candidates.stream()
                .map(shorts -> new JaccardSimilarityCalculator.SimilarityInput(
                        shorts.getId().toString(),
                        extractKeywords(shorts)
                ))
                .collect(Collectors.toList());

        List<JaccardSimilarityCalculator.SimilarityResult> results =
                JaccardSimilarityCalculator.calculateMultiple(baseKeywords, similarityInputs);

        return results.stream()
                .filter(result -> result.similarity() > 0)
                .limit(limit)
                .map(result -> {
                    Shorts recommended = candidateMap.get(result.shortsId());
                    return RecommendationResponse.from(recommended, result.similarity());
                })
                .collect(Collectors.toList());
    }

    private Set<String> extractKeywords(Shorts shorts) {
        return shorts.getShortsKeywords().stream()
                .map(sk -> sk.getKeyword().getName())
                .collect(Collectors.toSet());
    }
}