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
    @Column(length = 36)
    private String id; // shortsId

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

    @CreatedDate
    @Column
    private LocalDateTime createdAt;

    protected ShortsUploadSession() {
    }

    private ShortsUploadSession(
            String id,
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
            Integer expiresIn
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
    }

    public static ShortsUploadSession create(
            String shortsId,
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
            Integer expiresIn
    ) {
        return new ShortsUploadSession(
                shortsId,
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
                expiresIn
        );
    }

    public String getId() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
