package com.example.shortudy.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProfileImageUpdateRequest(
        @NotBlank(message = "이미지 키는 필수입니다.")
        @Pattern(
                regexp = "^profiles/.*",
                message = "올바른 프로필 이미지 경로가 아닙니다."
        )
        String newImageKey
) {
}
