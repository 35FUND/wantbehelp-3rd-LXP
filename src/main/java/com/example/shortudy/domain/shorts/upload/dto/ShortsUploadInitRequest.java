package com.example.shortudy.domain.shorts.upload.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ShortsUploadInitRequest(
        @NotNull(message = "body는 필수입니다.")
        @Valid
        Body body
) {
    public record Body(
            @NotBlank(message = "제목은 필수입니다.")
            @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
            String title,

            String description,

            @NotNull(message = "카테고리 ID는 필수입니다.")
            Long categoryId,

            List<@NotBlank(message = "키워드는 빈 값일 수 없습니다.") String> keywords,

            @NotBlank(message = "fileName은 필수입니다.")
            String fileName,

            @NotNull(message = "fileSize는 필수입니다.")
            @Positive(message = "fileSize는 0보다 커야 합니다.")
            Long fileSize,

            @NotBlank(message = "contentType은 필수입니다.")
            String contentType,

            String thumbnailFileName,

            Long thumbnailFileSize,

            String thumbnailContentType,

            @Positive(message = "durationSec는 0보다 커야 합니다.")
            Integer durationSec
    ) {
    }
}
