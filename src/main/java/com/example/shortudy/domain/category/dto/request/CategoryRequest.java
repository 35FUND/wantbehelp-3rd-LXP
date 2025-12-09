package com.example.shortudy.domain.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "카테고리 요청")
public record CategoryRequest(
        @Schema(description = "카테고리 이름", example = "프로그래밍")
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        String name
) {

}
