package com.example.shortudy.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        //TODO 현재 비밀번호와 새 비밀번호를 한번에 받아서 처리할까? (프론트 분들과 협의)
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요")
        @Size(min = 6, message = "비밀번호는 최소 6자 이상입니다.")
        String newPassword
) {
}
