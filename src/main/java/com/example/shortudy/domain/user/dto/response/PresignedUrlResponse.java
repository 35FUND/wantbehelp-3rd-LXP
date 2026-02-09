package com.example.shortudy.domain.user.dto.response;

public record PresignedUrlResponse(
        String url,
        String key
) {
}
