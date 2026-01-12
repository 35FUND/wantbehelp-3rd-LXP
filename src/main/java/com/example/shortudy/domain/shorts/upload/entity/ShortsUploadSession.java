package com.example.shortudy.domain.shorts.upload.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shorts_upload_session")
public class ShortsUploadSession {

    @Id
    private Long id; // shortId

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

    @Column(nullable = false, length = 500)
    private String objectKey;

    @Column(nullable = false)
    private Integer expiresIn;

    @Column(nullable = false)
    private Integer durationSec;

    @Column(nullable = false, length = 20)
    private String status;

    @Column
    private LocalDateTime uploadedAt;

    @CreatedDate
    @Column
    private LocalDateTime createdAt;

    protected ShortsUploadSession() {
    }

    private ShortsUploadSession(
            Long id,
            String uploadId,
            Long userId,
            Long categoryId,
            String title,
            String description,
            String keywords,
            String fileName,
            Long fileSize,
            String contentType,
            String objectKey,
            Integer expiresIn,
            Integer durationSec,
            String status,
            LocalDateTime uploadedAt
    ) {
        this.id = id;
        this.uploadId = uploadId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.objectKey = objectKey;
        this.expiresIn = expiresIn;
        this.durationSec = durationSec;
        this.status = status;
        this.uploadedAt = uploadedAt;
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
            String objectKey,
            Integer expiresIn,
            Integer durationSec
    ) {
        return new ShortsUploadSession(
                shortId,
                uploadId,
                userId,
                categoryId,
                title,
                description,
                keywords,
                fileName,
                fileSize,
                contentType,
                objectKey,
                expiresIn,
                durationSec,
                UploadStatus.INITIATED.name(),
                null
        );
    }

    public void markUploaded() {
        this.status = UploadStatus.COMPLETED.name();
        this.uploadedAt = LocalDateTime.now();
    }

    public enum UploadStatus {
        INITIATED,
        COMPLETED
    }

    public Long getId() {
        return id;
    }

    public String getUploadId() {
        return uploadId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
