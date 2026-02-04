package com.example.shortudy.domain.upload.controller;

import com.example.shortudy.domain.upload.dto.ShortsUploadCompleteRequest;
import com.example.shortudy.domain.upload.dto.ShortsUploadInitRequest;
import com.example.shortudy.domain.upload.dto.ShortsUploadInitResponse;
import com.example.shortudy.domain.upload.dto.ShortsUploadStatusResponse;
import com.example.shortudy.domain.upload.service.ShortsUploadCompleteService;
import com.example.shortudy.domain.upload.service.ShortsUploadInitService;
import com.example.shortudy.domain.upload.service.ShortsUploadStatusService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shorts")
public class ShortsUploadController {

    private final ShortsUploadInitService shortsUploadInitService;
    private final ShortsUploadCompleteService shortsUploadCompleteService;
    private final ShortsUploadStatusService shortsUploadStatusService;

    public ShortsUploadController(
            ShortsUploadInitService shortsUploadInitService,
            ShortsUploadCompleteService shortsUploadCompleteService,
            ShortsUploadStatusService shortsUploadStatusService
    ) {
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
        shortsUploadCompleteService.complete(
                shortId,
                userDetails.getId(),
                request
        );

        return ApiResponse.success("SUCCESS", "업로드가 완료되었습니다.", null);
    }

    /**
     * 업로드 상태 조회
     */
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