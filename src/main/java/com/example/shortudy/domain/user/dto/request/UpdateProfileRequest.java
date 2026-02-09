package com.example.shortudy.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        // 변경사항이 없을 경우가 있어 Null 체크는 하지 않음
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Size(min = 1, message = "최소 1자 이상이 필요합니다.")
        String nickName
) {
}
