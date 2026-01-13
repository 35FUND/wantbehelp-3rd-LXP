package com.example.shortudy.domain.recommendation.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JaccardSimilarityCalculator {

    private JaccardSimilarityCalculator() {}

    public static double calculateSimilarity(Set<String> set1, Set<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        // |A ∪ B| = |A| + |B| - |A ∩ B|
        int unionSize = set1.size() + set2.size() - intersection.size();

        return (double) intersection.size() / unionSize;
    }

    public static List<SimilarityResult> calculateMultiple(
            Set<String> baseKeywords,
            List<SimilarityInput> candidates
    ) {
        return candidates.stream()
                .map(candidate -> new SimilarityResult(
                        candidate.shortsId(),
                        calculateSimilarity(baseKeywords, candidate.keywords())
                ))
                .sorted(Comparator.comparingDouble(SimilarityResult::similarity).reversed())
                .toList();
    }

    public record SimilarityInput(Long shortsId, Set<String> keywords) {}
    public record SimilarityResult(Long shortsId, double similarity) {}
}