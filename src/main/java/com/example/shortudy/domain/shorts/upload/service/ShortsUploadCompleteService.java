package com.example.shortudy.domain.shorts.upload.service;

import com.example.shortudy.domain.shorts.upload.entity.ShortsUploadSession;
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
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ShortsUploadCompleteService {

    private final AwsProperties awsProperties;
    private final ShortsUploadSessionRepository uploadSessionRepository;

    public ShortsUploadCompleteService(AwsProperties awsProperties, ShortsUploadSessionRepository uploadSessionRepository) {
        this.awsProperties = awsProperties;
        this.uploadSessionRepository = uploadSessionRepository;
    }

    @Transactional
    public void complete(String shortsId, Long userId, String uploadId) {
        validateShortsId(shortsId);

        ShortsUploadSession session = uploadSessionRepository.findById(shortsId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_NOT_FOUND));

        if (uploadId == null || uploadId.isBlank() || session.getUploadId() == null || !session.getUploadId().equals(uploadId)) {
            throw new BaseException(ErrorCode.SHORTS_UPLOAD_SESSION_NOT_FOUND);
        }

        if (userId != null && session.getUserId() != null && !session.getUserId().equals(userId)) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        if (ShortsUploadSession.UploadStatus.COMPLETED.name().equals(session.getStatus())) {
            return;
        }

        validateNotExpired(session);
        validateUploadedObjectExists(session);
        session.markUploaded();
    }

    private void validateShortsId(String shortsId) {
        try {
            UUID.fromString(shortsId);
        } catch (Exception e) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "shortsId: 형식이 올바르지 않습니다.");
        }
    }

    private void validateNotExpired(ShortsUploadSession session) {
        if (session.getCreatedAt() == null || session.getExpiresIn() == null) {
            return;
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
            s3Client.headObject(headObjectRequest);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new BaseException(ErrorCode.SHORTS_UPLOAD_OBJECT_NOT_FOUND);
            }
            throw e;
        }
    }

    private String resolveBucket() {
        String bucket = awsProperties.getS3() == null ? null : awsProperties.getS3().getBucket();
        if (bucket == null || bucket.trim().isBlank()) {
            bucket = System.getenv("AWS_S3_BUCKET");
        }
        if (bucket == null || bucket.trim().isBlank()) {
            throw new BaseException(ErrorCode.AWS_S3_NOT_CONFIGURED, "S3 bucket 설정이 필요합니다.(aws.s3.bucket 또는 AWS_S3_BUCKET)");
        }
        return bucket.trim();
    }

    private Region resolveRegion() {
        String region = awsProperties.getRegion();
        if (region == null || region.trim().isBlank()) {
            region = System.getenv("AWS_REGION");
        }
        if (region == null || region.trim().isBlank()) {
            region = System.getenv("AWS_DEFAULT_REGION");
        }
        if (region == null || region.trim().isBlank()) {
            throw new BaseException(ErrorCode.AWS_S3_NOT_CONFIGURED, "AWS region 설정이 필요합니다.(aws.region 또는 AWS_REGION/AWS_DEFAULT_REGION)");
        }
        return Region.of(region.trim());
    }
}
