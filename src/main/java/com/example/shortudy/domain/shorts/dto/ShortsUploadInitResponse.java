package com.example.shortudy.domain.shorts.dto;

public record ShortsUploadInitResponse(
        Long shortId,
        String videouPresignedUrl,
        String thumbnailPresignedUrl,
        String uploadId,
        int expiresIn,
        long maxFileSize
) {
}
