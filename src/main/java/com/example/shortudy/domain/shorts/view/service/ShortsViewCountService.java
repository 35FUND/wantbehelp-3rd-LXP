package com.example.shortudy.domain.shorts.view.service;

import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.shorts.view.repository.RedisShortsViewCountRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;
import java.util.Set;


@Service
public class ShortsViewCountService {

    private static final Duration UNIQUE_TTL = Duration.ofHours(24);

    private final RedisShortsViewCountRepository viewCountRepository;
    private final ShortsRepository shortsRepository;

    public ShortsViewCountService(RedisShortsViewCountRepository viewCountRepository, ShortsRepository shortsRepository) {
        this.viewCountRepository = viewCountRepository;
        this.shortsRepository = shortsRepository;
    }

    // 조회수 증가 처리
    @Transactional
    public void increaseViewCount(Long shortId, String visitorId) {
        if (shortId == null || shortId <= 0) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "shortId: 값이 올바르지 않습니다.");
        }
        if (visitorId == null || visitorId.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "visitorId: 값이 올바르지 않습니다.");
        }

        boolean isNewView = viewCountRepository.markUniqueView(shortId, visitorId, UNIQUE_TTL);
        if (isNewView) {
            viewCountRepository.increaseViewCount(shortId);
        }
    }

    // Redis 누적 조회수 DB 반영
    @Transactional
    public void flushViewCounts() {
        Map<Long, Long> counts = viewCountRepository.findPendingViewCounts();
        if (counts.isEmpty()) {
            return;
        }

        for (Map.Entry<Long, Long> entry : counts.entrySet()) {
            shortsRepository.updateViewCount(entry.getKey(), entry.getValue());
        }
        viewCountRepository.clearPending(counts.keySet());
    }
}

