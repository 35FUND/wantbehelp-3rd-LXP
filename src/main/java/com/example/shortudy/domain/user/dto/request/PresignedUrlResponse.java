package com.example.shortudy.domain.user.dto.request;

public record PresignedUrlResponse(
        String url,
        String key
) {
}
