package com.example.shortudy.domain.category.controller;

import com.example.shortudy.domain.category.dto.request.CategoryRequest;
import com.example.shortudy.domain.category.dto.response.CategoryResponse;
import com.example.shortudy.domain.category.service.CategoryService;
import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.facade.ShortsQueryFacade;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ShortsQueryFacade shortsQueryFacade; // TODO : 이 부분 shortsQueryService로 이름 변경됨.

    public CategoryController(CategoryService categoryService, ShortsQueryFacade shortsQueryFacade) {
        this.categoryService = categoryService;
        this.shortsQueryFacade = shortsQueryFacade;
    }


    // 카테고리 생성.
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@RequestBody @Valid CategoryRequest request) {
        CategoryResponse created = categoryService.createCategory(request);

        // TODO : 카테고리 단일 조회 API가 필요한지 모르겠음.
//        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
//                .path("/{categoryId}")
//                .buildAndExpand(created.id())
//                .toUri();
//        return ResponseEntity.created(location).body(ApiResponse.success(created));

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    // 카테고리 전체 조회.
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> readAllCategories() {
        List<CategoryResponse> list = categoryService.readAllCategories();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    // 카테고리 단일 조회.
    // TODO : 카테고리 단일 조회 API가 필요한지 모르겠음. 추가적으로 현재 FE에서 사용하고 있지 않는 API
//    @GetMapping("/{categoryId}")
//    public ResponseEntity<ApiResponse<CategoryResponse>> readCategory(
//            @PathVariable Long categoryId) {
//        CategoryResponse resp = categoryService.readCategory(categoryId);
//        return ResponseEntity.ok(ApiResponse.success(resp));
//    }

    // 카테고리 업데이트.
    // TODO : 카테고리 업데이트는 필요 없을 것 같음.
    // TODO : "백엔드"라는 카테고리를 생성했다가 "poo"로 변경이 되면 안될 것 같음.
//    @PutMapping("/{categoryId}")
//    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable Long categoryId,
//                                                                        @RequestBody @Valid CategoryRequest request) {
//        CategoryResponse updated = categoryService.updateCategory(categoryId, request);
//        return ResponseEntity.ok(ApiResponse.success(updated));
//    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }

    /**
     * 카테고리별 숏츠 목록 조회
     */
    /*
    * TODO : 해당 API는 category에 대해서만 필터링이 가능해서 추후 삭제될 예정임.
    *       카테고리별 숏츠 조회는 /api/v1/shorts?categoryId={categoryId} 형태로 통합될 예정임.
    * */

    @Deprecated(since = "확장 가능성이 낮아 삭제 예정.")
    @GetMapping("/{categoryId}/shorts")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Page<ShortsResponse>>> getShortsByCategory(
            @PathVariable("categoryId") Long categoryId,
            @PageableDefault(size = 20, sort = "id", direction = DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long userId = (me != null) ? me.getId() : null;
        Page<ShortsResponse> response = shortsQueryFacade.getShortsByCategory(categoryId, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
