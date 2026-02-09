package com.example.shortudy.domain.upload.service;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.category.repository.CategoryRepository;
import com.example.shortudy.domain.keyword.service.KeywordService;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.domain.upload.dto.ShortsUploadInitRequest;
import com.example.shortudy.domain.upload.dto.ShortsUploadInitResponse;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.upload.entity.ShortsUploadSession;
import com.example.shortudy.domain.upload.entity.ShortsUploadSession.UploadStatus;
import com.example.shortudy.domain.upload.repository.ShortsUploadSessionRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.config.S3Service;
import com.example.shortudy.domain.user.dto.request.PresignedUrlResponse;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.shortudy.global.error.BaseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ShortsUploadInitService {

    private static final long MAX_FILE_SIZE_BYTES = 104_857_600L; // 100MB
    private static final long MAX_THUMBNAIL_SIZE_BYTES = 10_485_760L; // 10MB
    private static final int EXPIRES_IN_SECONDS = 3600;
    private static final String ALLOWED_CONTENT_TYPE = "video/mp4";
    private static final List<String> ALLOWED_THUMBNAIL_CONTENT_TYPES = List.of("image/jpeg", "image/png");


    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ShortsRepository shortsRepository;
    private final ShortsUploadSessionRepository uploadSessionRepository;
    private final ShortsService shortsService;
    private final KeywordService keywordService;
    private final S3Service s3Service;

    public ShortsUploadInitService(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ShortsRepository shortsRepository,
            ShortsUploadSessionRepository uploadSessionRepository,
            ShortsService shortsService,
            KeywordService keywordService,
            S3Service s3Service
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.shortsRepository = shortsRepository;
        this.uploadSessionRepository = uploadSessionRepository;
        this.shortsService = shortsService;
        this.keywordService = keywordService;
        this.s3Service = s3Service;
    }

    @Transactional
    public ShortsUploadInitResponse init(Long userId, ShortsUploadInitRequest.Body body) {
        validateFile(body.fileName(), body.fileSize(), body.contentType());
        validateThumbnail(body);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 같은 사용자의 기존 미완료 업로드는 재시도 시점에 즉시 정리한다.
        cleanupPreviousInitiatedUploads(userId);

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
                ShortsStatus.PENDING
        );

        // 키워드 저장
        if (body.keywords() != null) {
            body.keywords().forEach(k -> shorts.addKeyword(keywordService.getValidKeyword(k)));
        }


        Shorts savedShorts = shortsRepository.save(shorts);

        Long shortId = savedShorts.getId();
        String uploadId = "upload-" + UUID.randomUUID();

        // 1. 비디오 Presigned URL 발급 (글로벌 S3Service 활용)
        String videoKey = resolveVideoKey(shortId);
        PresignedUrlResponse videoPresigned = s3Service.getPresignedUrl(videoKey, body.contentType(), body.fileSize());

        // 2. 썸네일 Presigned URL 발급
        String thumbnailKey = resolveThumbnailKey(shortId, body.thumbnailFileName());
        String thumbnailUploadUrl = null;
        if (thumbnailKey != null) {
            PresignedUrlResponse thumbnailPresigned = s3Service.getPresignedUrl(thumbnailKey, body.thumbnailContentType(), body.thumbnailFileSize());
            thumbnailUploadUrl = thumbnailPresigned.url();
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
                videoPresigned.url(),
                thumbnailUploadUrl,
                uploadId,
                EXPIRES_IN_SECONDS,
                MAX_FILE_SIZE_BYTES
        );
    }

    // 재업로드 시 이전 INITIATED 세션과 연관된 고아 쇼츠를 즉시 정리한다.
    // 정상 UX에서는 단건이 대부분이지만, 중복 클릭/재시도 등 예외 상황을 고려해 목록 기반으로 처리한다.
    private void cleanupPreviousInitiatedUploads(Long userId) {
        List<ShortsUploadSession> previousSessions =
                uploadSessionRepository.findByUserIdAndStatus(userId, UploadStatus.INITIATED);

        for (ShortsUploadSession previousSession : previousSessions) {
            Long previousShortId = previousSession.getShortId();
            if (previousShortId != null) {
                shortsService.deleteOrphanPendingShorts(previousShortId);
            }
            uploadSessionRepository.delete(previousSession);
        }
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
        if (normalizedFileName.contains("/") || normalizedFileName.contains("\\")) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "thumbnail: 파일명에 경로 구분자는 허용되지 않습니다.");
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

    // 썸네일 키 생성
    private String resolveThumbnailKey(Long shortId, String thumbnailFileName) {
        if (!hasText(thumbnailFileName)) {
            return null;
        }
        String normalizedFileName = thumbnailFileName.trim();
        return "thumbnails/" + shortId + "/" + normalizedFileName;
    }

    // 비디오 키 생성
    private String resolveVideoKey(Long shortId) {
        return "videos/" + shortId + ".mp4";
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
