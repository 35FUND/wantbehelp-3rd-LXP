package com.example.shortudy.domain.shorts.controller;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUpdateRequest;
import com.example.shortudy.domain.shorts.facade.ShortsQueryFacade;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ShortsController {

    private final ShortsService shortsService;
    private final ShortsQueryFacade shortsQueryFacade;

    @GetMapping("/shorts/{shortsId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsResponse> getShortsDetails(
            @PathVariable Long shortsId
    ) {
        ShortsResponse result = shortsQueryFacade.getShortsDetails(shortsId);
        return ApiResponse.success(result);
    }

    @GetMapping("/shorts")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getShortsList(
            @PageableDefault(size = 8, sort = "id", direction = ASC) Pageable pageable
    ) {
        Page<ShortsResponse> response = shortsQueryFacade.getShortsList(pageable);
        return ApiResponse.success(response);
    }

    @PatchMapping("/shorts/{shortsId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsResponse> updateShorts(
            @PathVariable Long shortsId,
            @RequestBody @Valid ShortsUpdateRequest request
    ) {
        ShortsResponse response = shortsService.updateShorts(shortsId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/shorts/{shortsId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteShorts(@PathVariable Long shortsId) {
        shortsService.deleteShorts(shortsId);
        return ApiResponse.success(null);
    }

    /**
     * 인기 숏츠 목록 조회
     */
    @GetMapping("/shorts/popular")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getPopularShorts(
            @RequestParam(required = false, defaultValue = "30") Integer days,
            @PageableDefault(size = 20, sort = "id", direction = DESC) Pageable pageable
    ) {
        Page<ShortsResponse> response = shortsQueryFacade.getPopularShorts(days, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 내 쇼츠 목록 조회 (로그인 사용자)
     */
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getMyShorts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "id", direction = DESC) Pageable pageable
    ) {
        Page<ShortsResponse> response = shortsQueryFacade.getMyShorts(userDetails.getId(), pageable);
        return ApiResponse.success(response);
    }
}
