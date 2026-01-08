package com.example.shortudy.domain.shorts.controller;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUpdateRequest;
import com.example.shortudy.domain.shorts.dto.ShortsUploadRequest;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shorts")
public class ShortsController {

    private final ShortsService shortsService;

    public ShortsController(ShortsService shortsService) {
        this.shortsService = shortsService;
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

