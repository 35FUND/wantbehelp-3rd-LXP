package com.example.shortudy.domain.shorts.dto;

import java.time.LocalDateTime;

/**
 * 숏츠 업로드 상태 조회 응답 DTO
 *
 * - 업로드 상태는 서버 기준으로 관리되며, S3 업로드 완료(객체 존재) 확인 이후 확정된다.
 * - 본 MVP에서는 세션 상태(INITIATED/COMPLETED)를 PENDING/COMPLETED로 매핑하여 반환한다.
 */
public record ShortsUploadStatusResponse(
        Long shortId,
        String status,
        String videoUrl,
        String thumbnailUrl,
        Integer durationSec,
        LocalDateTime uploadedAt,
        LocalDateTime completedAt,
        String errorMessage
) {
}


