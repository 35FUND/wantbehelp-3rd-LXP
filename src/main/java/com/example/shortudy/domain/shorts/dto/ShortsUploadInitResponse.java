package com.example.shortudy.domain.shorts.dto;

public record ShortsUploadInitResponse(
        String shortsId,
        String uploadUrl,
        String uploadId,
        int expiresIn,
        long maxFileSize
) {
}
