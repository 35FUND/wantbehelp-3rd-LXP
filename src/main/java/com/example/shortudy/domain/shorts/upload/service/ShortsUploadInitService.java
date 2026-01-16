package com.example.shortudy.domain.shorts.upload.service;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.category.repository.CategoryRepository;
import com.example.shortudy.domain.shorts.upload.dto.ShortsUploadInitRequest;
import com.example.shortudy.domain.shorts.upload.dto.ShortsUploadInitResponse;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.shorts.upload.entity.ShortsUploadSession;
import com.example.shortudy.domain.shorts.upload.repository.ShortsUploadSessionRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.config.AwsProperties;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static com.example.shortudy.domain.shorts.entity.ShortsStatus.DRAFT;

@Service
@Transactional(readOnly = true)
public class ShortsUploadInitService {

    private static final long MAX_FILE_SIZE_BYTES = 104_857_600L; // 100MB
    private static final long MAX_THUMBNAIL_SIZE_BYTES = 10_485_760L; // 10MB
    private static final int EXPIRES_IN_SECONDS = 3600;
    private static final String ALLOWED_CONTENT_TYPE = "video/mp4";
    private static final List<String> ALLOWED_THUMBNAIL_CONTENT_TYPES = List.of("image/jpeg", "image/png");


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

    @PostConstruct
    public void validateAwsConfiguration() {
        // 서버 기동 시 S3 설정을 검증한다.
        String bucket = resolveBucket();
        Region region = resolveRegion();
        StaticCredentialsProvider credentialsProvider = resolveCredentials();

        try (S3Client s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(S3Configuration.builder().checksumValidationEnabled(false).build())
                .build()) {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (RuntimeException e) {
            // 로컬 개발/테스트 환경을 위해 예외를 던지지 않고 로그만 출력
            System.err.println("⚠️ AWS S3 설정 검증 실패 (무시 가능): " + e.getMessage());
            // throw new BaseException(ErrorCode.AWS_S3_NOT_CONFIGURED, "S3 설정 검증에 실패했습니다. 버킷/리전/자격증명을 확인해주세요. " + e.getMessage());
        }
    }

    @Transactional
    public ShortsUploadInitResponse init(Long userId, ShortsUploadInitRequest.Body body) {
        validateFile(body.fileName(), body.fileSize(), body.contentType());
        validateThumbnail(body);

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
                DRAFT
        );

        Shorts savedShorts = shortsRepository.save(shorts);

        Long shortId = savedShorts.getId();
        String uploadId = "upload-" + UUID.randomUUID();

        String objectKey = "videos/" + shortId + ".mp4";

        String bucket = resolveBucket();
        Region region = resolveRegion();

        PresignedPutObjectRequest videoPresigned = presignPut(bucket, objectKey, body.contentType(), region);

        String thumbnailObjectKey = resolveThumbnailKey(shortId, body.thumbnailFileName());
        String thumbnailUploadUrl = null;
        if (thumbnailObjectKey != null) {
            PresignedPutObjectRequest thumbnailPresigned = presignPut(bucket, thumbnailObjectKey, body.thumbnailContentType(), region);
            thumbnailUploadUrl = thumbnailPresigned.url().toString();
        }

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
                body.thumbnailFileName(),
                body.thumbnailFileSize(),
                body.thumbnailContentType(),
                EXPIRES_IN_SECONDS,
                body.durationSec()
        );
        uploadSessionRepository.save(session);

        return new ShortsUploadInitResponse(
                shortId,
                videoPresigned.url().toString(),
                thumbnailUploadUrl,
                uploadId,
                EXPIRES_IN_SECONDS,
                MAX_FILE_SIZE_BYTES
        );
    }

    // 업로드용 비디오 파일 검증
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

    // 썸네일 파일 요청 검증
    private void validateThumbnail(ShortsUploadInitRequest.Body body) {
        boolean hasFileName = hasText(body.thumbnailFileName());
        boolean hasFileSize = body.thumbnailFileSize() != null;
        boolean hasContentType = hasText(body.thumbnailContentType());

        if (!hasFileName && !hasFileSize && !hasContentType) {
            return;
        }

        if (!hasFileName || !hasFileSize || !hasContentType) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "thumbnail: 파일 정보가 충분하지 않습니다.");
        }

        if (body.thumbnailFileSize() > MAX_THUMBNAIL_SIZE_BYTES) {
            throw new BaseException(ErrorCode.SHORTS_FILE_TOO_LARGE, "thumbnail: 파일 크기가 허용 범위를 초과했습니다.");
        }

        String normalizedContentType = body.thumbnailContentType().trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_THUMBNAIL_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new BaseException(ErrorCode.SHORTS_UNSUPPORTED_FILE_TYPE, "thumbnail: 지원하지 않는 파일 형식입니다.");
        }

        String normalizedFileName = body.thumbnailFileName().trim().toLowerCase(Locale.ROOT);
        if (!(normalizedFileName.endsWith(".jpg") || normalizedFileName.endsWith(".jpeg") || normalizedFileName.endsWith(".png"))) {
            throw new BaseException(ErrorCode.SHORTS_UNSUPPORTED_FILE_TYPE, "thumbnail: 확장자가 올바르지 않습니다.");
        }
    }

    // 키워드 목록 정리
    private String joinKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "키워드는 필수입니다.");
        }
        return keywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(30)
                .reduce((a, b) -> a + "," + b)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT, "키워드는 필수입니다."));
    }

    // Presigned URL 생성
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
        
        StaticCredentialsProvider credentialsProvider = resolveCredentials();

        try (S3Presigner presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(S3Configuration.builder().checksumValidationEnabled(false).build())
                .build()) {
            return presigner.presignPutObject(presignRequest);
        }
    }

    // S3 버킷 설정 확인
    private String resolveBucket() {
        String bucket = trimToNull(awsProperties.getS3().getBucket());
        if (bucket == null) {
            throw new BaseException(ErrorCode.AWS_S3_NOT_CONFIGURED, "S3 bucket 설정이 필요합니다.(aws.s3.bucket)");
        }
        return bucket;
    }


    // S3 리전 설정 확인
    private Region resolveRegion() {
        String region = trimToNull(awsProperties.getRegion());
        if (region == null) {
            throw new BaseException(ErrorCode.AWS_S3_NOT_CONFIGURED, "AWS region 설정이 필요합니다.(aws.region)");
        }
        return Region.of(region);
    }
    
    // AWS 자격 증명 생성
    private StaticCredentialsProvider resolveCredentials() {
        String accessKey = trimToNull(awsProperties.getCredentials().getAccessKey());
        String secretKey = trimToNull(awsProperties.getCredentials().getSecretKey());

        if (accessKey == null || secretKey == null) {
            throw new BaseException(ErrorCode.AWS_S3_NOT_CONFIGURED, "AWS 자격 증명(accessKey, secretKey) 설정이 필요합니다.");
        }

        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }


    // 썸네일 키 생성
    private String resolveThumbnailKey(Long shortId, String thumbnailFileName) {
        if (!hasText(thumbnailFileName)) {
            return null;
        }
        String extension = resolveExtension(thumbnailFileName);
        return "thumbnails/" + shortId + extension;
    }

    // 파일 확장자 추출
    private String resolveExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        return fileName.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    // 문자열 유효성 검사
    private boolean hasText(String value) {
        return value != null && !value.trim().isBlank();
    }

    // 문자열 트리밍 후 null 처리
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

}
