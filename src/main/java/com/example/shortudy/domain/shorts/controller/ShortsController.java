package com.example.shortudy.domain.shorts.controller;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUpdateRequest;
import com.example.shortudy.domain.shorts.dto.ShortsUploadInitRequest;
import com.example.shortudy.domain.shorts.dto.ShortsUploadInitResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUploadRequest;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.domain.shorts.upload.service.ShortsUploadInitService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/shorts")
public class ShortsController {

    private final ShortsService shortsService;
    private final ShortsUploadInitService shortsUploadInitService;

    public ShortsController(ShortsService shortsService, ShortsUploadInitService shortsUploadInitService) {
        this.shortsService = shortsService;
        this.shortsUploadInitService = shortsUploadInitService;
    }


    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsUploadInitResponse> initUpload(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ShortsUploadInitRequest request
    ) {
        ShortsUploadInitResponse response = shortsUploadInitService.init(userDetails.getId(), request.body());
        return ApiResponse.success("SUCCESS", "업로드 URL이 생성되었습니다.", response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShortsResponse> uploadShorts(@RequestBody @Valid ShortsUploadRequest request) {
        ShortsResponse response = shortsService.uploadShorts(request);
        return ApiResponse.success(response);
    }


    @GetMapping("/{shortId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getShortsDetails(
            @PathVariable Long shortId,
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<ShortsResponse> response = shortsService.getShortsDetailsWithPaging(shortId, pageable);
        return ApiResponse.success(response);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getShortsList(
            @PageableDefault(size = 8, sort = "id", direction = org.springframework.data.domain.Sort.Direction.ASC) Pageable pageable) {
        Page<ShortsResponse> response = shortsService.getShortsList(pageable);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{shortId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsResponse> updateShorts(
            @PathVariable Long shortId,
            @RequestBody @Valid ShortsUpdateRequest request) {
        ShortsResponse response = shortsService.updateShorts(shortId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{shortId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteShorts(
            @PathVariable Long shortId) {
        shortsService.deleteShorts(shortId);
        return ApiResponse.success(null);
    }
}

