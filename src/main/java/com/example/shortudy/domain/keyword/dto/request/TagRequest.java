package com.example.shortudy.domain.keyword.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TagRequest (
        @NotBlank(message = "태그 이름은 필수입니다.")
        String name
){
}
