package com.example.shortudy.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "Email cannot be blank")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, message = "비밀번호는 6자 이상입니다.")
        String password,

        @NotBlank(message = "Nickname cannot be blank")
        @Size(min = 1, message = "닉네임은 1자 이상입니다.")
        String nickname
) {
}
