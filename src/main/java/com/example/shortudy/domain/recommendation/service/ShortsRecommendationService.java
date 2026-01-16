package com.example.shortudy.domain.recommendation.service;

import com.example.shortudy.domain.recommendation.dto.response.RecommendationResponse;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.global.error.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.shortudy.domain.shorts.entity.ShortsStatus.PUBLISHED;
import static com.example.shortudy.global.error.ErrorCode.SHORTS_NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class ShortsRecommendationService {

    private final ShortsRepository shortsRepository;

    public ShortsRecommendationService(ShortsRepository shortsRepository) {
        this.shortsRepository = shortsRepository;
    }

    public RecommendationResponse getRecommendations(Long shortsId, int offset, int limit) {
        // 1. 기준 Shorts 조회
        Shorts baseShorts = shortsRepository.findById(shortsId)
                .orElseThrow(() -> new BaseException(SHORTS_NOT_FOUND, "해당 숏츠를 찾을 수 없습니다."));

        Set<String> baseKeywords = extractKeywords(baseShorts);

        // 2. 후보 Shorts 조회 (기준 제외, PUBLISHED 상태만)
        List<Shorts> candidates = shortsRepository.findRecommendationCandidates(
                shortsId,
                ShortsStatus.PUBLISHED.name());

        // 3. 모든 유사도 계산 (한 번만!)
        List<JaccardSimilarityCalculator.SimilarityResult> allResults =
                calculateAllSimilarities(baseKeywords, candidates);

        // 4. 유사도 > 0인 것만 필터링
        List<JaccardSimilarityCalculator.SimilarityResult> filteredResults = allResults.stream()
                .filter(result -> result.similarity() > 0)
                .toList();

        int totalCount = filteredResults.size();

        // 5. 페이징 적용
        List<JaccardSimilarityCalculator.SimilarityResult> pagedResults = filteredResults.stream()
                .skip(offset)
                .limit(limit)
                .toList();

        // 6. Shorts Map 생성 (조회 최적화)
        Set<Long> pagedShortsIds = pagedResults.stream()
                .map(JaccardSimilarityCalculator.SimilarityResult::shortsId)
                .collect(Collectors.toSet());

        Map<Long, Shorts> shortsMap = candidates.stream()
                .filter(shorts -> pagedShortsIds.contains(shorts.getId()))
                .collect(Collectors.toMap(Shorts::getId, Function.identity()));

        // 7. Response 생성
        List<RecommendationResponse.ShortsRecommendation> recommendations = pagedResults.stream()
                .map(result -> {
                    Shorts recommended = shortsMap.get(result.shortsId());
                    return RecommendationResponse.ShortsRecommendation.from(
                            recommended,
                            result.similarity()
                    );
                })
                .toList();

        return RecommendationResponse.of(recommendations, offset, limit, totalCount);
    }

    private List<JaccardSimilarityCalculator.SimilarityResult> calculateAllSimilarities(
            Set<String> baseKeywords,
            List<Shorts> candidates
    ) {
        List<JaccardSimilarityCalculator.SimilarityInput> inputs = candidates.stream()
                .map(shorts -> new JaccardSimilarityCalculator.SimilarityInput(
                        shorts.getId(),
                        extractKeywords(shorts)
                ))
                .toList();

        return JaccardSimilarityCalculator.calculateMultiple(baseKeywords, inputs);
    }

    private Set<String> extractKeywords(Shorts shorts) {
        return shorts.getShortsKeywords().stream()
                .map(sk -> sk.getKeyword().getDisplayName())
                .collect(Collectors.toSet());
    }
}