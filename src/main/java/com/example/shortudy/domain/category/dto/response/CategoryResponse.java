package com.example.shortudy.domain.category.dto.response;

import com.example.shortudy.domain.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponse{
    private final Long id;
    private final String name;

    public static CategoryResponse of(Category category){
        return new CategoryResponse(category.getId(), category.getName());
    }
}

