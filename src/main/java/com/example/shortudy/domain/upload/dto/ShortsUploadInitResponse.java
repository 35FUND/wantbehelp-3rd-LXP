package com.example.shortudy.domain.upload.dto;

public record ShortsUploadInitResponse(
        Long shortId,
        String videoPresignedUrl,
        String thumbnailPresignedUrl,
        String uploadId,
        int expiresIn,
        long maxFileSize
) {
}
