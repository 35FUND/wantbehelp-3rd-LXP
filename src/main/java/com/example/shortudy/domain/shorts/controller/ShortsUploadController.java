package com.example.shortudy.domain.shorts.controller;

import com.example.shortudy.domain.shorts.upload.dto.ShortsUploadCompleteRequest;
import com.example.shortudy.domain.shorts.upload.dto.ShortsUploadInitRequest;
import com.example.shortudy.domain.shorts.upload.dto.ShortsUploadInitResponse;
import com.example.shortudy.domain.shorts.upload.dto.ShortsUploadStatusResponse;
import com.example.shortudy.domain.shorts.upload.service.ShortsUploadCompleteService;
import com.example.shortudy.domain.shorts.upload.service.ShortsUploadInitService;
import com.example.shortudy.domain.shorts.upload.service.ShortsUploadStatusService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shorts")
public class ShortsUploadController {

    private final ShortsUploadInitService shortsUploadInitService;
    private final ShortsUploadCompleteService shortsUploadCompleteService;
    private final ShortsUploadStatusService shortsUploadStatusService;

    public ShortsUploadController(ShortsUploadInitService shortsUploadInitService, ShortsUploadCompleteService shortsUploadCompleteService, ShortsUploadStatusService shortsUploadStatusService) {
        this.shortsUploadInitService = shortsUploadInitService;
        this.shortsUploadCompleteService = shortsUploadCompleteService;
        this.shortsUploadStatusService = shortsUploadStatusService;
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

    @PostMapping("/{shortId}/upload-complete")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> uploadComplete(
            @PathVariable Long shortId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ShortsUploadCompleteRequest request
    ) {
        shortsUploadCompleteService.complete(shortId, userDetails.getId(), request.uploadId());
        return ApiResponse.success("SUCCESS", "업로드가 완료되었습니다.", null);
    }

    @GetMapping("/{shortId}/upload-status")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsUploadStatusResponse> getUploadStatus(
            @PathVariable Long shortId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ShortsUploadStatusResponse response = shortsUploadStatusService.getStatus(shortId, userDetails.getId());
        return ApiResponse.success("SUCCESS", "업로드 상태 조회에 성공했습니다.", response);
    }
}