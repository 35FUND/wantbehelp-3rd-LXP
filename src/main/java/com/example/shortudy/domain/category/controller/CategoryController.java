package com.example.shortudy.domain.category.controller;

import com.example.shortudy.domain.category.dto.request.CategoryRequest;
import com.example.shortudy.domain.category.dto.response.CategoryResponse;
import com.example.shortudy.domain.category.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/category")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.create(request));
    }

    @GetMapping("/(categoryId)")
    public ResponseEntity<CategoryResponse> read(@RequestParam("categoryId") Long categoryId) {

        return ResponseEntity.ok(categoryService.read(categoryId));
    }
}

