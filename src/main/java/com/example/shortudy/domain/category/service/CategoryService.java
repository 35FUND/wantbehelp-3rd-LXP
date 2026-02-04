package com.example.shortudy.domain.category.service;

import com.example.shortudy.domain.category.dto.request.CategoryRequest;
import com.example.shortudy.domain.category.dto.response.CategoryResponse;
import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.category.repository.CategoryRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)  // 기본적으로 읽기 전용
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional  // 쓰기 작업은 readOnly=false
    public CategoryResponse createCategory(CategoryRequest request) {
        // 중복 카테고리명 체크
        if (categoryRepository.existsByName(request.name())) {
            throw new BaseException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        Category created = categoryRepository.save(new Category(request.name()));
        return CategoryResponse.of(created);
    }

    // TODO : 카테고리 단일 조회 API를 사용하지 않으므로 주석 처리
//    public CategoryResponse readCategory(Long id) {
//        Category found = categoryRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다: " + id));
//        return CategoryResponse.of(found);
//    }

    public List<CategoryResponse> readAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::of)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        Category toErase = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다: " + id));
        categoryRepository.delete(toErase);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다: " + id));

        // 다른 카테고리와 이름 중복 체크
        categoryRepository.findByName(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("이미 존재하는 카테고리입니다: " + request.name());
                });

        category.updateName(request.name());
        return CategoryResponse.of(category);
    }
}

