package com.example.shortudy.domain.shorts.upload.service;

import com.example.shortudy.domain.shorts.dto.ShortsUploadCompleteRequest;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.shorts.upload.entity.ShortsUploadSession;
import com.example.shortudy.domain.shorts.upload.entity.ShortsUploadSession.UploadStatus;
import com.example.shortudy.domain.shorts.upload.repository.ShortsUploadSessionRepository;
import com.example.shortudy.global.config.AwsProperties;
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
    private final AwsProperties awsProperties;

    public ShortsUploadCompleteService(
            ShortsRepository shortsRepository,
            ShortsUploadSessionRepository uploadSessionRepository,
            AwsProperties awsProperties
    ) {
        this.shortsRepository = shortsRepository;
        this.uploadSessionRepository = uploadSessionRepository;
        this.awsProperties = awsProperties;
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

        // 썸네일 URL 결정 (클라이언트가 안 보냈으면 Lambda 생성 경로 예측)
        String finalThumbnailUrl = resolveThumbnailUrl(shortId, request.thumbnailUrl());

        shorts.updateVideoUrl(request.videoUrl());
        shorts.updateShorts(null, null, finalThumbnailUrl, null, ShortsStatus.PUBLISHED);

        session.updateUploadedUrls(request.videoUrl(), finalThumbnailUrl);
        session.markUploaded();
    }

    private String resolveThumbnailUrl(Long shortId, String requestedUrl) {
        if (requestedUrl != null && !requestedUrl.isBlank()) {
            return requestedUrl;
        }
        // 썸네일이 없으면 AWS Lambda가 생성할 경로를 미리 저장
        String bucket = awsProperties.getS3().getBucket();
        String region = awsProperties.getRegion();
        return String.format("https://%s.s3.%s.amazonaws.com/thumbnails/%d.jpg", bucket, region, shortId);
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
