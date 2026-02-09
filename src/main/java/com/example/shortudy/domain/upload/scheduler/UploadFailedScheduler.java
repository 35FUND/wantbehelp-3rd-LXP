package com.example.shortudy.domain.upload.scheduler;

import com.example.shortudy.domain.upload.service.ShortsUploadCompleteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UploadFailedScheduler {

    private static final Logger log = LoggerFactory.getLogger(UploadFailedScheduler.class);


    private final ShortsUploadCompleteService shortsUploadCompleteService;


    public UploadFailedScheduler(ShortsUploadCompleteService shortsUploadCompleteService){
        this.shortsUploadCompleteService = shortsUploadCompleteService;

    }

    // 1시간마다 만료된 INITIATED 세션을 정리한다.
    @Scheduled(cron = "${shorts.upload.cleanup.cron:0 0 * * * *}")
    public void deleteUploadFailedSessions() {
        long deletedCount = shortsUploadCompleteService.deleteExpiredInitiatedSessions();
        if (deletedCount > 0) {
            log.info("업로드 미완료 세션 정리 완료: {}건 삭제", deletedCount);
        }
    }


}
