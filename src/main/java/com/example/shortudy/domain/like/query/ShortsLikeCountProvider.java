package com.example.shortudy.domain.like.query;

import com.example.shortudy.domain.like.repository.ShortsLikeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ShortsLikeCountProvider {

    private final ShortsLikeRepository shortsLikeRepository;

    public  ShortsLikeCountProvider(ShortsLikeRepository shortsLikeRepository) {
        this.shortsLikeRepository = shortsLikeRepository;
    }

    public Map<Long, Long> likeCountMap(List<Long> shortsIds) {
        if (shortsIds.isEmpty()) return Map.of();

        return shortsLikeRepository.countLikesByShortsIds(shortsIds).stream()
                .collect(Collectors.toMap(
                        ShortsLikeRepository.ShortsLikeCountProjection::getShortsId,
                        ShortsLikeRepository.ShortsLikeCountProjection::getCnt
                ));
    }
}
