package com.example.shortudy.domain.recommendation.service;

import com.example.shortudy.domain.recommendation.dto.response.RecommendationResponse;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)

public class ShortsRecommendationService {

    private final ShortsRepository shortsRepository;

    public ShortsRecommendationService(ShortsRepository shortsRepository) {
        this.shortsRepository = shortsRepository;
    }

    public List<RecommendationResponse> getRecommendations(Long shortsId, int limit) {

        Shorts baseShorts = shortsRepository.findById(shortsId)
                .orElseThrow(() -> new IllegalArgumentException("쇼츠를 찾을 수 없습니다."));
        Set<String> baseKeywords = extractKeywords(baseShorts);

        List<Shorts> candidates = shortsRepository.findByIdNot(shortsId);

        Map<Long, Shorts> candidateMap = candidates.stream()
                .collect(Collectors.toMap(
                        Shorts::getId,
                        Function.identity()
                ));

        List<JaccardSimilarityCalculator.SimilarityInput> similarityInputs = candidates.stream()
                .map(shorts -> new JaccardSimilarityCalculator.SimilarityInput(
                        shorts.getId(),
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