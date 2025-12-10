package com.example.shortudy.domain.category.service;

import com.example.shortudy.domain.category.dto.request.CategoryRequest;
import com.example.shortudy.domain.category.dto.response.CategoryResponse;
import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {

        Category created = categoryRepository.save(
                new Category(
                        request.name()
                )
        );

        return CategoryResponse.of(created);
    }

    @Transactional
    public CategoryResponse read(Long categoryId) {

        Category found = categoryRepository.findById(categoryId).orElseThrow(()->
                new EntityNotFoundException("Not Found"));

        return CategoryResponse.of(found);
    }
}

