package com.example.shortudy.domain.upload.dto;

import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import java.time.LocalDateTime;

/**
 * 숏츠 업로드 상태 조회 응답 DTO
 *
 * - 업로드 상태는 서버 기준으로 관리되며, S3 업로드 완료(객체 존재) 확인 이후 확정된다.
 * - 업로드 파이프라인 상태(uploadStatus)와 숏츠 비즈니스 상태(shortsStatus)를 분리해 반환한다.
 */
public record ShortsUploadStatusResponse(
        Long shortId,
        String uploadStatus,
        ShortsStatus shortsStatus,
        String videoUrl,
        String thumbnailUrl,
        Integer durationSec,
        LocalDateTime uploadedAt,
        LocalDateTime completedAt,
        String errorMessage
) {
}


