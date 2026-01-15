package com.example.shortudy.domain.shorts.view.scheduler;

import com.example.shortudy.domain.shorts.view.service.ShortsViewCountService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ShortsViewCountFlushScheduler {

    private final ShortsViewCountService shortsViewCountService;

    public ShortsViewCountFlushScheduler(ShortsViewCountService shortsViewCountService) {
        this.shortsViewCountService = shortsViewCountService;
    }

    // Redis 조회수를 DB에 주기적으로 반영
    @Scheduled(fixedDelayString = "${shorts.view.flush-interval-ms:60000}")
    public void flushViewCounts() {
        shortsViewCountService.flushViewCounts();
    }
}
