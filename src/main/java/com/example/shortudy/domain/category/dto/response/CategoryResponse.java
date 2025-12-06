package com.example.shortudy.domain.category.dto.response;

import com.example.shortudy.domain.category.dto.request.CategoryRequest;
import com.example.shortudy.domain.category.entity.Category;

public record CategoryResponse(
        Long id,
        String name
) {

    public static CategoryResponse of(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}

