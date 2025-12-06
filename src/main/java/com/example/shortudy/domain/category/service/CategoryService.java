package com.example.shortudy.domain.category.service;

import com.example.shortudy.domain.category.dto.request.CategoryRequest;
import com.example.shortudy.domain.category.dto.response.CategoryResponse;
import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        Category created = categoryRepository.save(
                new Category(
                        request.name()
                )
        );

        return CategoryResponse.of(created);
    }

    @Transactional
    public CategoryResponse readCategory(Long id) {

        Category found = categoryRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Not Found"));

        return CategoryResponse.of(found);
    }

    @Transactional
    public List<CategoryResponse> readAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::of)
                .toList();
    }

    @Transactional
    public void delete(Long id) {

        Category toErase = categoryRepository.findById(id)
                .orElseThrow(() -> new  EntityNotFoundException("Not Found"));

        categoryRepository.deleteById(toErase.getId());
    }
}

