package com.example.shortudy.domain.category.controller;

import com.example.shortudy.domain.category.dto.request.CategoryRequest;
import com.example.shortudy.domain.category.dto.response.CategoryResponse;
import com.example.shortudy.domain.category.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> readAll() {

        return ResponseEntity
                .ok(categoryService.readAll());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> read(@PathVariable Long categoryId) {

        return ResponseEntity.ok(categoryService.read(categoryId));
    }

    @DeleteMapping("{categoryId}")
    public void deleteCategory(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
    }
}

