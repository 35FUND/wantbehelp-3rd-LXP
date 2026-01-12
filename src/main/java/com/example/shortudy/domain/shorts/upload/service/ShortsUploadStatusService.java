package com.example.shortudy.domain.shorts.upload.service;

import com.example.shortudy.domain.shorts.dto.ShortsUploadStatusResponse;
import com.example.shortudy.domain.shorts.upload.entity.ShortsUploadSession;
import com.example.shortudy.domain.shorts.upload.repository.ShortsUploadSessionRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 숏츠 업로드 상태 조회 서비스
 *
 * - 클라이언트(프론트)가 업로드 화면에서 상태 폴링/조회 용도로 사용한다.
 * - 실제 업로드(PUT)는 프론트가 S3로 직접 수행한다.
 */
@Service
@Transactional(readOnly = true)
public class ShortsUploadStatusService {

    private final ShortsUploadSessionRepository uploadSessionRepository;

    public ShortsUploadStatusService(ShortsUploadSessionRepository uploadSessionRepository) {
        this.uploadSessionRepository = uploadSessionRepository;
    }

    public ShortsUploadStatusResponse getStatus(Long shortId, Long requesterUserId) {
        if (shortId == null || shortId <= 0) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "shortId: 값이 올바르지 않습니다.");
        }

        ShortsUploadSession session = uploadSessionRepository.findById(shortId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_NOT_FOUND));

        if (requesterUserId != null && session.getUserId() != null && !session.getUserId().equals(requesterUserId)) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        LocalDateTime expiresAt = null;
        if (session.getCreatedAt() != null && session.getExpiresIn() != null) {
            expiresAt = session.getCreatedAt().plusSeconds(session.getExpiresIn());
        }

        String status = mapStatus(session.getStatus());
        int progress = "COMPLETED".equals(status) ? 100 : 0;

        return new ShortsUploadStatusResponse(
                shortId,
                status,
                progress,
                expiresAt,
                session.getUploadedAt()
        );
    }

    private String mapStatus(String rawStatus) {
        if (rawStatus == null) {
            return "PENDING";
        }

        if (ShortsUploadSession.UploadStatus.COMPLETED.name().equals(rawStatus)) {
            return "COMPLETED";
        }

        return "PENDING";
    }
}
