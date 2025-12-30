package com.example.shortudy.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @Email
        @NotBlank(message = "Email cannot be blank")
        String email,

        @NotBlank(message = "Password cannot be blank")
        String password,

        @NotBlank(message = "Nickname cannot be blank")
        String nickname,

        //TODO profile은 null 가능? default profileUrl 존재? 이전에 userUrl 어떻게 사용?
        String profileUrl
) {
}
