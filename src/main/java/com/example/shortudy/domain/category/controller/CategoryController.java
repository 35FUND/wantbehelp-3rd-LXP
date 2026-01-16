package com.example.shortudy.domain.category.controller;

import com.example.shortudy.domain.category.dto.request.CategoryRequest;
import com.example.shortudy.domain.category.dto.response.CategoryResponse;
import com.example.shortudy.domain.category.service.CategoryService;
import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ShortsService shortsService;

    public CategoryController(CategoryService categoryService, ShortsService shortsService) {
        this.categoryService = categoryService;
        this.shortsService = shortsService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@RequestBody @Valid CategoryRequest request) {
        CategoryResponse created = categoryService.createCategory(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{categoryId}")
                .buildAndExpand(created.id()) //생성된 객체의 ID를 사용
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.success(created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> readAllCategories() {
        List<CategoryResponse> list = categoryService.readAllCategories();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> readCategory(
            @PathVariable Long categoryId) {
        CategoryResponse resp = categoryService.readCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable Long categoryId,
                                                                        @RequestBody @Valid CategoryRequest request) {
        CategoryResponse updated = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.noContent().build();
    }

    // 신규 추가: 카테고리별 숏츠 목록 조회

    /**
     * 카테고리별 숏츠 목록 조회
     *
     * @param categoryId 카테고리 ID
     * @param pageable 페이징 정보 (page, size, sort)
     * @return 해당 카테고리의 숏츠 목록
     */
    @GetMapping("/{categoryId}/shorts")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Page<ShortsResponse>>> getShortsByCategory(
            @PathVariable("categoryId") Long categoryId,
            @PageableDefault(size = 20, sort = "id", direction = DESC) Pageable pageable
    ) {
        Page<ShortsResponse> response = shortsService.getShortsByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}