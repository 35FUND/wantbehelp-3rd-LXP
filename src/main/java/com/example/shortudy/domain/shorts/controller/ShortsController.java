package com.example.shortudy.domain.shorts.controller;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUpdateRequest;
import com.example.shortudy.domain.shorts.facade.ShortsQueryFacade;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.domain.shorts.view.service.ShortsViewCountService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/shorts")
@RequiredArgsConstructor
public class ShortsController {

    private final ShortsService shortsService;
    private final ShortsQueryFacade shortsQueryFacade;
    private final ShortsViewCountService viewCountService;

    @GetMapping("/{shortsId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsResponse> getShortsDetails(
            @PathVariable Long shortsId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        // 1. 조회수 증가 로직 (IP 또는 UserID 기반 중복 방지)
        String visitorId = (userDetails != null) ?
                String.valueOf(userDetails.getId()) : getClientIp(request);
        viewCountService.increaseViewCount(shortsId, visitorId);

        // 2. 통합 정보 조회
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        ShortsResponse result = shortsQueryFacade.getShortsDetails(shortsId, userId);
        return ApiResponse.success(result);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getMyShorts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "id", direction = DESC) Pageable pageable
    ) {
        Page<ShortsResponse> response = shortsQueryFacade.getMyShorts(userDetails.getId(), pageable);
        return ApiResponse.success(response);
    }

    /**
     * 인기 숏츠 목록 조회
     */
    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getPopularShorts(
            @RequestParam(required = false, defaultValue = "30") Integer days,
            @PageableDefault(size = 20, sort = "id", direction = DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long userId = (me != null) ? me.getId() : null;
        Page<ShortsResponse> response = shortsQueryFacade.getPopularShorts(days, pageable, userId);
        return ApiResponse.success(response);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getShortsList(
            @PageableDefault(size = 8, sort = "id", direction = ASC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long userId = (me != null) ? me.getId() : null;
        Page<ShortsResponse> response = shortsQueryFacade.getShortsList(pageable, userId);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{shortsId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsResponse> updateShorts(
            @PathVariable Long shortsId,
            @RequestBody @Valid ShortsUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ShortsResponse response = shortsService.updateShorts(shortsId, request, me.getId());
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{shortsId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteShorts(
            @PathVariable Long shortsId,
            @AuthenticationPrincipal CustomUserDetails me) {
        shortsService.deleteShorts(shortsId, me.getId());
        return ApiResponse.success(null);
    }
}
