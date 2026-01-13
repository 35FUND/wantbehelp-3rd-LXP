package com.example.shortudy.domain.keyword.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KeywordRequest(
        @NotBlank(message = "키워드는 비어있을 수 없습니다.")
        String name
){
}
