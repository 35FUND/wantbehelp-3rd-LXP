package com.example.shortudy.domain.shorts.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 숏츠 업로드 완료 알림 요청 DTO
 *
 * - 클라이언트가 S3 PUT 업로드를 성공한 뒤, 백엔드에 "업로드 완료"를 알리는 용도
 */
public record ShortsUploadCompleteRequest(
        @NotBlank(message = "uploadId는 필수입니다.")
        String uploadId // 프론트에서 주는 메타데이터 반영하여 수정 예정
) {
}
