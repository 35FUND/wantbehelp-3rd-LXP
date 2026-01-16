package com.example.shortudy.domain.shorts.controller;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUpdateRequest;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1")
public class ShortsController {

    private final ShortsService shortsService;

    public ShortsController(ShortsService shortsService) {
        this.shortsService = shortsService;
    }

    /**
     * 내 쇼츠 목록 조회 (로그인 사용자)
     * GET /api/v1/shorts/me
     */
    @GetMapping("/shorts/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getMyShorts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "id", direction = DESC) Pageable pageable
    ) {
        Page<ShortsResponse> response = shortsService.getMyShorts(userDetails.getId(), pageable);
        return ApiResponse.success(response);
    }

    /**
     * 인기 숏츠 목록 조회
     * GET /api/v1/shorts/popular
     */
    @GetMapping("/shorts/popular")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getPopularShorts(
            @RequestParam(required = false, defaultValue = "30") Integer days,
            @PageableDefault(size = 20, sort = "id", direction = DESC) Pageable pageable
    ) {
        Page<ShortsResponse> response = shortsService.getPopularShorts(days, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 쇼츠 상세 조회
     * GET /api/v1/shorts/{shortsId}
     * 단일 쇼츠 정보만 반환 (Page 아님!)
     */
    @GetMapping("/shorts/{shortsId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsResponse> getShortsDetails(
            @PathVariable Long shortsId
    ) {
        ShortsResponse result = shortsService.getShortsDetails(shortsId);
        return ApiResponse.success(result);
    }

    /**
     * 쇼츠 목록 조회
     * GET /api/v1/shorts
     */
    @GetMapping("/shorts")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getShortsList(
            @PageableDefault(size = 8, sort = "id", direction = ASC) Pageable pageable
    ) {
        Page<ShortsResponse> response = shortsService.getShortsList(pageable);
        return ApiResponse.success(response);
    }

    /**
     * 쇼츠 수정
     * PATCH /api/v1/shorts/{shortsId}
     */
    @PatchMapping("/shorts/{shortsId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsResponse> updateShorts(
            @PathVariable Long shortsId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ShortsUpdateRequest request
    ) {
        ShortsResponse response = shortsService.updateShorts(shortsId, request);
        return ApiResponse.success(response);
    }

    /**
     * 쇼츠 삭제
     * DELETE /api/v1/shorts/{shortsId}
     */
    @DeleteMapping("/shorts/{shortsId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteShorts(
            @PathVariable Long shortsId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        shortsService.deleteShorts(shortsId);
        return ApiResponse.success(null);
    }
}