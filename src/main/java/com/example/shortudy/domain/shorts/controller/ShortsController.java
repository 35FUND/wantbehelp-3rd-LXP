package com.example.shortudy.domain.shorts.controller;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUpdateRequest;
import com.example.shortudy.domain.shorts.dto.ShortsUploadCompleteRequest;
import com.example.shortudy.domain.shorts.dto.ShortsUploadInitRequest;
import com.example.shortudy.domain.shorts.dto.ShortsUploadInitResponse;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.domain.shorts.upload.service.ShortsUploadCompleteService;
import com.example.shortudy.domain.shorts.dto.ShortsUploadStatusResponse;
import com.example.shortudy.domain.shorts.upload.service.ShortsUploadInitService;
import com.example.shortudy.domain.shorts.upload.service.ShortsUploadStatusService;
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
    private final ShortsUploadCompleteService shortsUploadCompleteService;
    private final ShortsUploadStatusService shortsUploadStatusService;

    public ShortsController(
            ShortsService shortsService,
            ShortsUploadInitService shortsUploadInitService,
            ShortsUploadCompleteService shortsUploadCompleteService,
            ShortsUploadStatusService shortsUploadStatusService
    ) {
        this.shortsService = shortsService;
        this.shortsUploadInitService = shortsUploadInitService;
        this.shortsUploadCompleteService = shortsUploadCompleteService;
        this.shortsUploadStatusService = shortsUploadStatusService;
    }

    /**
     * 업로드용 Pre-signed URL 발급
     * - 실제 PUT 업로드는 클라이언트가 S3로 직접 수행한다.
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsUploadInitResponse> initUpload(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ShortsUploadInitRequest request
    ) {
        ShortsUploadInitResponse response = shortsUploadInitService.init(userDetails.getId(), request.body());
        return ApiResponse.success("SUCCESS", "업로드 URL이 생성되었습니다.", response);
    }

    /**
     * 업로드 완료 알림
     * - 서버 관점(A안): S3에 객체가 실제로 존재(HEAD)하는지 확인 후 완료 처리한다.
     */
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

    @GetMapping("/{shortId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getShortsDetails(
            @PathVariable Long shortId,
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ShortsResponse> result = shortsService.getShortsDetailsWithPaging(shortId, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getShortsList(
            @PageableDefault(size = 8, sort = "id", direction = org.springframework.data.domain.Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ShortsResponse> response = shortsService.getShortsList(pageable);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{shortId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsResponse> updateShorts(
            @PathVariable Long shortId,
            @RequestBody @Valid ShortsUpdateRequest request
    ) {
        ShortsResponse response = shortsService.updateShorts(shortId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{shortId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteShorts(@PathVariable Long shortId) {
        shortsService.deleteShorts(shortId);
        return ApiResponse.success(null);
    }
}

