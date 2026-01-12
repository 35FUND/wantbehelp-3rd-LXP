package com.example.shortudy.domain.category.dto.response;

import com.example.shortudy.domain.category.entity.Category;
import lombok.Getter;

@Getter
public class CategoryResponse{
    private final Long id;
    private final String name;

    public CategoryResponse(Long id, String name){
        this.id = id;
        this.name = name;
    }
    public static CategoryResponse of(Category category){
        return new CategoryResponse(category.getId(), category.getName());
    }
}

