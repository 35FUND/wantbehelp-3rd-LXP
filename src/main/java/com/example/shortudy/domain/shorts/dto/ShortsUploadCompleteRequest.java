package com.example.shortudy.domain.shorts.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 숏츠 업로드 완료 알림 요청 DTO
 *
 * - 클라이언트가 업로드를 완료한 뒤, 재생/썸네일 URL을 전달한다.
 * - MVP 단계에서는 전달된 URL을 완료 기준으로 사용한다.
 */
public record ShortsUploadCompleteRequest(
        @NotBlank(message = "uploadId는 필수입니다.")
        String uploadId,

        @NotBlank(message = "videoUrl은 필수입니다.")
        String videoUrl,

        @NotBlank(message = "thumbnailUrl은 필수입니다.")
        String thumbnailUrl
) {
}

