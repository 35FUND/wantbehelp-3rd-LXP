package com.example.shortudy.domain.tag.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "태그 요청")
public record TagRequest (
        @Schema(description = "태그 이름", example = "Java")
        @NotBlank(message = "태그 이름은 필수입니다.")
        String name
){
}
