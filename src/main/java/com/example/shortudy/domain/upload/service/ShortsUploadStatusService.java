package com.example.shortudy.domain.upload.service;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.upload.dto.ShortsUploadStatusResponse;
import com.example.shortudy.domain.upload.entity.ShortsUploadSession;
import com.example.shortudy.domain.upload.entity.ShortsUploadSession.UploadStatus;
import com.example.shortudy.domain.upload.repository.ShortsUploadSessionRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ShortsRepository shortsRepository;

    public ShortsUploadStatusService(
            ShortsUploadSessionRepository uploadSessionRepository,
            ShortsRepository shortsRepository
    ) {
        this.uploadSessionRepository = uploadSessionRepository;
        this.shortsRepository = shortsRepository;
    }

    public ShortsUploadStatusResponse getStatus(Long shortId, Long requesterUserId) {
        if (shortId == null || shortId <= 0) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "shortId: 값이 올바르지 않습니다.");
        }

        ShortsUploadSession session = uploadSessionRepository.findByShortId(shortId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_NOT_FOUND));

        if (requesterUserId != null && session.getUserId() != null && !session.getUserId().equals(requesterUserId)) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        if (session.getCreatedAt() == null || session.getExpiresIn() == null) {
            throw new BaseException(ErrorCode.INTERNAL_ERROR, "업로드 세션의 시간 정보가 누락되었습니다.");
        }
        Shorts shorts = shortsRepository.findById(shortId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));

        String uploadStatus = mapUploadStatus(session.getStatus());

        return new ShortsUploadStatusResponse(
                shortId,
                uploadStatus,
                shorts.getStatus(),
                shorts.getStatusDescription(),
                session.getVideoUrl(),
                session.getThumbnailUrl(),
                session.getDurationSec(),
                session.getUploadedAt(),
                session.getCompletedAt(),
                null
        );
    }


    private String mapUploadStatus(UploadStatus rawStatus) {
        if (rawStatus == null) {
            return UploadStatus.INITIATED.name();
        }
        return rawStatus.name();
    }
}
