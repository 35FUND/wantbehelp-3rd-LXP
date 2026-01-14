package com.example.shortudy.domain.shorts.upload.service;

import com.example.shortudy.domain.shorts.dto.ShortsUploadCompleteRequest;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.shorts.upload.entity.ShortsUploadSession;
import com.example.shortudy.domain.shorts.upload.entity.ShortsUploadSession.UploadStatus;
import com.example.shortudy.domain.shorts.upload.repository.ShortsUploadSessionRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ShortsUploadCompleteService {

    private final ShortsRepository shortsRepository;
    private final ShortsUploadSessionRepository uploadSessionRepository;

    public ShortsUploadCompleteService(
            ShortsRepository shortsRepository,
            ShortsUploadSessionRepository uploadSessionRepository
    ) {
        this.shortsRepository = shortsRepository;
        this.uploadSessionRepository = uploadSessionRepository;
    }

    @Transactional
    public void complete(Long shortId, Long userId, ShortsUploadCompleteRequest request) {
        validateShortId(shortId);

        ShortsUploadSession session = uploadSessionRepository.findById(shortId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_NOT_FOUND));

        if (request.uploadId() == null || request.uploadId().isBlank()
                || session.getUploadId() == null
                || !session.getUploadId().equals(request.uploadId())) {
            throw new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_NOT_FOUND);
        }

        if (userId != null && session.getUserId() != null && !session.getUserId().equals(userId)) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        if (UploadStatus.COMPLETED == session.getStatus()) {
            return;
        }

        validateNotExpired(session);

        Shorts shorts = shortsRepository.findById(shortId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));

        // MVP 단계: 프론트에서 전달한 URL을 완료 기준으로 저장한다.
        shorts.updateVideoUrl(request.videoUrl());
        shorts.updateShorts(null, null, request.thumbnailUrl(), null, ShortsStatus.PUBLISHED);

        session.updateUploadedUrls(request.videoUrl(), request.thumbnailUrl());
        session.markUploaded();
    }

    private void validateShortId(Long shortId) {
        if (shortId == null || shortId <= 0) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "shortId: 값이 올바르지 않습니다.");
        }
    }

    private void validateNotExpired(ShortsUploadSession session) {
        if (session.getCreatedAt() == null || session.getExpiresIn() == null) {
            throw new BaseException(ErrorCode.INTERNAL_ERROR, "업로드 세션의 시간 정보가 누락되었습니다.");
        }

        LocalDateTime expiresAt = session.getCreatedAt().plusSeconds(session.getExpiresIn());
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_EXPIRED);
        }
    }
}
