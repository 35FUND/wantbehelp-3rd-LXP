package com.example.shortudy.domain.shorts.upload.service;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.category.repository.CategoryRepository;
import com.example.shortudy.domain.shorts.dto.ShortsUploadInitRequest;
import com.example.shortudy.domain.shorts.dto.ShortsUploadInitResponse;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.shorts.upload.entity.ShortsUploadSession;
import com.example.shortudy.domain.shorts.upload.repository.ShortsUploadSessionRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.config.AwsProperties;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ShortsUploadInitService {

    private static final long MAX_FILE_SIZE_BYTES = 104_857_600L; // 100MB
    private static final int EXPIRES_IN_SECONDS = 3600;
    private static final String ALLOWED_CONTENT_TYPE = "video/mp4";

    private final AwsProperties awsProperties;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ShortsRepository shortsRepository;
    private final ShortsUploadSessionRepository uploadSessionRepository;

    public ShortsUploadInitService(
            AwsProperties awsProperties,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ShortsRepository shortsRepository,
            ShortsUploadSessionRepository uploadSessionRepository
    ) {
        this.awsProperties = awsProperties;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.shortsRepository = shortsRepository;
        this.uploadSessionRepository = uploadSessionRepository;
    }

    @Transactional
    public ShortsUploadInitResponse init(Long userId, ShortsUploadInitRequest.Body body) {
        validateFile(body.fileName(), body.fileSize(), body.contentType());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(body.categoryId())
                .orElseThrow(() -> new BaseException(ErrorCode.CATEGORY_NOT_FOUND));

        // 프론트가 추출한 메타데이터(durationSec 등)를 그대로 저장한다.
        Shorts shorts = new Shorts(
                user,
                category,
                body.title(),
                body.description(),
                null,
                null,
                body.durationSec(),
                ShortsStatus.DRAFT
        );
        Shorts savedShorts = shortsRepository.save(shorts);

        Long shortId = savedShorts.getId();
        String uploadId = "upload-" + UUID.randomUUID();

        String objectKey = "videos/" + shortId + ".mp4";

        String bucket = resolveBucket();
        Region region = resolveRegion();

        PresignedPutObjectRequest presigned = presignPut(bucket, objectKey, body.contentType(), region);

        String keywords = joinKeywords(body.keywords());
        ShortsUploadSession session = ShortsUploadSession.create(
                shortId,
                uploadId,
                userId,
                body.categoryId(),
                body.title(),
                body.description(),
                keywords,
                body.fileName(),
                body.fileSize(),
                body.contentType(),
                objectKey,
                EXPIRES_IN_SECONDS,
                body.durationSec()
        );
        uploadSessionRepository.save(session);

        return new ShortsUploadInitResponse(
                shortId,
                presigned.url().toString(),
                uploadId,
                EXPIRES_IN_SECONDS,
                MAX_FILE_SIZE_BYTES
        );
    }

    private void validateFile(String fileName, Long fileSize, String contentType) {
        if (fileSize != null && fileSize > MAX_FILE_SIZE_BYTES) {
            throw new BaseException(ErrorCode.SHORTS_FILE_TOO_LARGE);
        }

        String normalizedContentType = contentType == null ? null : contentType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPE.equals(normalizedContentType)) {
            throw new BaseException(ErrorCode.SHORTS_UNSUPPORTED_FILE_TYPE);
        }

        String normalizedFileName = fileName == null ? null : fileName.trim().toLowerCase(Locale.ROOT);
        if (normalizedFileName == null || !normalizedFileName.endsWith(".mp4")) {
            throw new BaseException(ErrorCode.SHORTS_UNSUPPORTED_FILE_TYPE);
        }
    }

    private String joinKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        return keywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(30)
                .reduce((a, b) -> a + "," + b)
                .orElse(null);
    }

    private PresignedPutObjectRequest presignPut(String bucket, String key, String contentType, Region region) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(EXPIRES_IN_SECONDS))
                .putObjectRequest(putObjectRequest)
                .build();

        try (S3Presigner presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .serviceConfiguration(S3Configuration.builder().checksumValidationEnabled(false).build())
                .build()) {
            return presigner.presignPutObject(presignRequest);
        }
    }

    private String resolveBucket() {
        String bucket = trimToNull(awsProperties.getS3().getBucket());
        if (bucket == null) {
            bucket = trimToNull(System.getenv("AWS_S3_BUCKET"));
        }
        if (bucket == null) {
            throw new BaseException(ErrorCode.AWS_S3_NOT_CONFIGURED, "S3 bucket 설정이 필요합니다.(aws.s3.bucket 또는 AWS_S3_BUCKET)");
        }
        return bucket;
    }

    private Region resolveRegion() {
        String region = trimToNull(awsProperties.getRegion());
        if (region == null) {
            region = trimToNull(System.getenv("AWS_REGION"));
        }
        if (region == null) {
            region = trimToNull(System.getenv("AWS_DEFAULT_REGION"));
        }
        if (region == null) {
            throw new BaseException(ErrorCode.AWS_S3_NOT_CONFIGURED, "AWS region 설정이 필요합니다.(aws.region 또는 AWS_REGION/AWS_DEFAULT_REGION)");
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
}
