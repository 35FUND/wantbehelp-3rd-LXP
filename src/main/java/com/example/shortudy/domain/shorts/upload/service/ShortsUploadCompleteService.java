package com.example.shortudy.domain.shorts.upload.service;

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
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ShortsUploadCompleteService {

    private final AwsProperties awsProperties;
    private final ShortsRepository shortsRepository;
    private final ShortsUploadSessionRepository uploadSessionRepository;

    public ShortsUploadCompleteService(
            AwsProperties awsProperties,
            ShortsRepository shortsRepository,
            ShortsUploadSessionRepository uploadSessionRepository
    ) {
        this.awsProperties = awsProperties;
        this.shortsRepository = shortsRepository;
        this.uploadSessionRepository = uploadSessionRepository;
    }

    @Transactional
    public void complete(Long shortId, Long userId, String uploadId) {
        validateShortId(shortId);

        ShortsUploadSession session = uploadSessionRepository.findById(shortId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_NOT_FOUND));

        if (uploadId == null || uploadId.isBlank() || session.getUploadId() == null || !session.getUploadId().equals(uploadId)) {
            throw new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_NOT_FOUND);
        }

        if (userId != null && session.getUserId() != null && !session.getUserId().equals(userId)) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        if (UploadStatus.COMPLETED == session.getStatus()) {
            return;
        }

        validateNotExpired(session);
        validateUploadedObjectExists(session);

        // S3에 실제 객체가 존재하는 것이 확인되면, 최종 URL을 확정한다.
        Shorts shorts = shortsRepository.findById(shortId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));

        String bucket = resolveBucket();
        Region region = resolveRegion();
        String finalVideoUrl = buildS3ObjectUrl(bucket, region, session.getObjectKey());

        shorts.updateVideoUrl(finalVideoUrl);
        shorts.updateShorts(null, null, null, null, ShortsStatus.PUBLISHED);

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

    /**
     * 업로드 완료를 "서버 관점"에서 확정하기 위한 검증
     *
     * - 클라이언트의 PUT 성공 응답만으로는 서버가 업로드를 확정할 수 없다.
     * - 따라서 complete 호출 시점에 S3에 객체가 실제로 존재하는지 HEAD로 확인한다.
     */
    private void validateUploadedObjectExists(ShortsUploadSession session) {
        String bucket = resolveBucket();
        Region region = resolveRegion();

        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(session.getObjectKey())
                .build();

        try (S3Client s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {
            var headObject = s3Client.headObject(headObjectRequest);

            if (session.getFileSize() != null && headObject.contentLength() != null && headObject.contentLength() != session.getFileSize()) {
                throw new BaseException(ErrorCode.INVALID_INPUT, "fileSize: 업로드된 파일 크기가 요청과 일치하지 않습니다.");
            }

            if (session.getContentType() != null && headObject.contentType() != null && !headObject.contentType().equalsIgnoreCase(session.getContentType())) {
                throw new BaseException(ErrorCode.INVALID_INPUT, "contentType: 업로드된 파일 타입이 요청과 일치하지 않습니다.");
            }
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new BaseException(ErrorCode.SHORTS_UPLOAD_OBJECT_NOT_FOUND);
            }
            throw e;
        }
    }

    private String resolveBucket() {
        String bucket = trimToNull(awsProperties.getS3().getBucket());
        if (bucket == null) {
            throw new BaseException(ErrorCode.AWS_S3_NOT_CONFIGURED, "S3 bucket 설정이 필요합니다.(aws.s3.bucket)");
        }
        return bucket;
    }

    private Region resolveRegion() {
        String region = trimToNull(awsProperties.getRegion());
        if (region == null) {
            throw new BaseException(ErrorCode.AWS_S3_NOT_CONFIGURED, "AWS region 설정이 필요합니다.(aws.region)");
        }
        return Region.of(region);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String buildS3ObjectUrl(String bucket, Region region, String objectKey) {
        return "https://" + bucket + ".s3." + region.id() + ".amazonaws.com/" + objectKey;
    }
}
