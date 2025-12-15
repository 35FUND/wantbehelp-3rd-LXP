package com.example.shortudy.domain.category.dto.response;

import com.example.shortudy.domain.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리 응답")
public record CategoryResponse(
        @Schema(description = "카테고리 ID", example = "1")
        Long id,

        @Schema(description = "카테고리 이름", example = "프로그래밍")
        String name,

        @Schema(description = "부모 카테고리 ID (null이면 최상위)", example = "null")
        Long parentId
) {

    public static CategoryResponse of(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getParentId()
        );
    }
}

