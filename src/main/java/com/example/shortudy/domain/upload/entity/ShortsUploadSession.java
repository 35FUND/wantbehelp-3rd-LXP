package com.example.shortudy.domain.upload.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shorts_upload_session")
public class ShortsUploadSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long shortId;

    @Column(nullable = false, length = 64)
    private String uploadId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(length = 500)
    private String videoUrl;

    @Column(length = 255)
    private String thumbnailFileName;

    @Column
    private Long thumbnailFileSize;

    @Column(length = 100)
    private String thumbnailContentType;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false)
    private Integer expiresIn;

    @Column(nullable = false)
    private Integer durationSec;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UploadStatus status;

    @CreatedDate
    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime uploadedAt;

    @Column
    private LocalDateTime completedAt;


    protected ShortsUploadSession() {
    }

    private ShortsUploadSession(
            Long id,
            Long shortId, // shortId 파라미터 추가
            String uploadId,
            Long userId,
            Long categoryId,
            String title,
            String description,
            String keywords,
            String fileName,
            Long fileSize,
            String contentType,
            String videoUrl,
            String thumbnailFileName,
            Long thumbnailFileSize,
            String thumbnailContentType,
            String thumbnailUrl,
            Integer expiresIn,
            Integer durationSec,
            UploadStatus status,
            LocalDateTime createdAt,
            LocalDateTime uploadedAt,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.shortId = shortId; // shortId 할당
        this.uploadId = uploadId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.videoUrl = videoUrl;
        this.thumbnailFileName = thumbnailFileName;
        this.thumbnailFileSize = thumbnailFileSize;
        this.thumbnailContentType = thumbnailContentType;
        this.thumbnailUrl = thumbnailUrl;
        this.expiresIn = expiresIn;
        this.durationSec = durationSec;
        this.status = status;
        this.createdAt = createdAt;
        this.uploadedAt = uploadedAt;
        this.completedAt = completedAt;
    }

    public static ShortsUploadSession create(
            Long shortId,
            String uploadId,
            Long userId,
            Long categoryId,
            String title,
            String description,
            String keywords,
            String fileName,
            Long fileSize,
            String contentType,
            String thumbnailFileName,
            Long thumbnailFileSize,
            String thumbnailContentType,
            Integer expiresIn,
            Integer durationSec
    ) {
        return new ShortsUploadSession(
                null, // id (PK)는 null로 설정하여 자동 생성되게 함
                shortId, // shortId 전달
                uploadId,
                userId,
                categoryId,
                title,
                description,
                keywords,
                fileName,
                fileSize,
                contentType,
                null,
                thumbnailFileName,
                thumbnailFileSize,
                thumbnailContentType,
                null,
                expiresIn,
                durationSec,
                UploadStatus.INITIATED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }

    // 업로드 완료 상태 전환
    public void markUploaded() {
        this.status = UploadStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    // 업로드 완료 URL 저장
    public void updateUploadedUrls(String videoUrl, String thumbnailUrl) {
        if (videoUrl != null && !videoUrl.isBlank()) {
            this.videoUrl = videoUrl;
        }
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }

    public enum UploadStatus {
        INITIATED,
        COMPLETED
    }
}
