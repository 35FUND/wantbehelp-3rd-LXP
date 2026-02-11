package com.example.shortudy.domain.upload.service;

import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.domain.upload.entity.ShortsUploadSession;
import com.example.shortudy.domain.upload.entity.ShortsUploadSession.UploadStatus;
import com.example.shortudy.domain.upload.repository.ShortsUploadSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ShortsUploadCleanupService {

    private final ShortsUploadSessionRepository uploadSessionRepository;
    private final ShortsService shortsService;

    public ShortsUploadCleanupService(
            ShortsUploadSessionRepository uploadSessionRepository,
            ShortsService shortsService
    ) {
        this.uploadSessionRepository = uploadSessionRepository;
        this.shortsService = shortsService;
    }

    /**
     * 만료된 미완료(INITIATED) 업로드 세션과 연관 고아 숏츠를 정리한다.
     */
    @Transactional
    public int cleanupStaleInitiatedSessions(LocalDateTime threshold) {
        List<ShortsUploadSession> staleSessions =
                uploadSessionRepository.findByStatusAndCreatedAtBefore(UploadStatus.INITIATED, threshold);

        for (ShortsUploadSession staleSession : staleSessions) {
            Long shortId = staleSession.getShortId();
            if (shortId != null) {
                shortsService.deleteOrphanPendingShorts(shortId);
            }
            uploadSessionRepository.delete(staleSession);
        }

        return staleSessions.size();
    }
}
