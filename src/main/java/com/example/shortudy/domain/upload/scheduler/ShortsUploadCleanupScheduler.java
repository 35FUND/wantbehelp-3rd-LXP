package com.example.shortudy.domain.upload.scheduler;

import com.example.shortudy.domain.upload.service.ShortsUploadCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ShortsUploadCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(ShortsUploadCleanupScheduler.class);

    private final ShortsUploadCleanupService shortsUploadCleanupService;

    @Value("${shorts.upload.cleanup.retention-days:7}")
    private int retentionDays;

    public ShortsUploadCleanupScheduler(ShortsUploadCleanupService shortsUploadCleanupService) {
        this.shortsUploadCleanupService = shortsUploadCleanupService;
    }

    // 하루 1회, retentionDays(기본 7일) 이상 지난 미완료(INITIATED/INV) 업로드 세션을 정리한다.
    @Scheduled(cron = "${shorts.upload.cleanup.cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void cleanupStaleUploads() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        int deletedCount = shortsUploadCleanupService.cleanupStaleInitiatedSessions(threshold);

        if (deletedCount > 0) {
            log.info("미완료 업로드 세션 정리 완료 - deletedCount={}, threshold={}", deletedCount, threshold);
        }
    }
}
