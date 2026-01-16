package com.example.shortudy.domain.shorts.upload.service;

import com.example.shortudy.domain.shorts.upload.dto.ShortsUploadCompleteRequest;
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

    /**
     * 업로드 완료 처리
     *
     * @param shortId 숏츠 ID
     * @param userId 사용자 ID
     * @param request 업로드 완료 요청 (uploadId 포함)
     */
    @Transactional
    public void complete(Long shortId, Long userId, ShortsUploadCompleteRequest request) {
        validateShortId(shortId);

        // 1. 세션 조회
        ShortsUploadSession session = uploadSessionRepository.findById(shortId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_NOT_FOUND));

        // 2. 검증 (UploadID, UserID, 만료 여부)
        validateSession(session, request.uploadId(), userId);

        // 3. 이미 완료된 상태면 중복 처리 방지 (Idempotency)
        if (UploadStatus.COMPLETED == session.getStatus()) {
            return;
        }

        // 4. Shorts 엔티티 조회
        Shorts shorts = shortsRepository.findById(shortId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));

        // 클라이언트가 준 URL을 믿지 않고, 서버가 직접 경로 생성
        // (Init 단계에서 정한 규칙대로 URL을 다시 조립)
        String videoUrl = generateS3Url("videos", shortId, "mp4"); // 확장자는 mp4 고정 가정
        String thumbnailUrl = generateS3Url("thumbnails", shortId, "jpg"); // 썸네일은 jpg 고정 가정

        // 5. 엔티티 및 세션 업데이트
        shorts.updateVideoUrl(videoUrl);
        shorts.updateShorts(null, null, thumbnailUrl, null, ShortsStatus.PUBLISHED);

        session.updateUploadedUrls(videoUrl, thumbnailUrl);
        session.markUploaded();
    }

    /**
     * URL 생성 로직 분리 (S3 버킷 URL 조합)
     */
    private String generateS3Url(String folder, Long id, String extension) {
        String bucket = awsProperties.getS3().getBucket();
        String region = awsProperties.getRegion();
        // 예: https://my-bucket.s3.ap-northeast-2.amazonaws.com/shorts/100.mp4
        return String.format("https://%s.s3.%s.amazonaws.com/%s/%d.%s", bucket, region, folder, id, extension);
    }

    /**
     * ShortId 검증
     */
    private void validateShortId(Long shortId) {
        if (shortId == null || shortId <= 0) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "shortId: 값이 올바르지 않습니다.");
        }
    }

    /**
     * 업로드 세션 검증
     */
    private void validateSession(ShortsUploadSession session, String requestUploadId, Long userId) {
        // Upload ID 검증
        if (requestUploadId == null || requestUploadId.isBlank()
                || session.getUploadId() == null
                || !session.getUploadId().equals(requestUploadId)) {
            throw new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_NOT_FOUND);
        }

        // User ID 검증
        if (userId != null && session.getUserId() != null && !session.getUserId().equals(userId)) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        // 만료 시간 검증
        if (session.getCreatedAt() == null || session.getExpiresIn() == null) {
            throw new BaseException(ErrorCode.INTERNAL_ERROR, "업로드 세션의 시간 정보가 누락되었습니다.");
        }
        LocalDateTime expiresAt = session.getCreatedAt().plusSeconds(session.getExpiresIn());
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_EXPIRED);
        }
    }
}