package com.example.shortudy.domain.shorts.dto;

public record ShortsUploadInitResponse(
        Long shortId,
        String uploadUrl,
        String uploadId,
        int expiresIn,
        long maxFileSize
) {
}
