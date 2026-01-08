package com.example.shortudy.domain.category.dto.response;

import com.example.shortudy.domain.category.entity.Category;

public record CategoryResponse(
        Long id,
        String name,
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

