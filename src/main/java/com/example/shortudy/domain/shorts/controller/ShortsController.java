package com.example.shortudy.domain.shorts.controller;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUpdateRequest;
import com.example.shortudy.domain.shorts.dto.ShortsUploadRequest;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Shorts", description = "숏폼 영상 API")
@RestController
@RequestMapping("/api/v1/shorts")
public class ShortsController {

    private final ShortsService shortsService;

    public ShortsController(ShortsService shortsService) {
        this.shortsService = shortsService;
    }

    @Operation(summary = "숏폼 업로드", description = "새로운 숏폼 영상을 업로드합니다. (로그인 필요)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "업로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShortsResponse> uploadShorts(@RequestBody @Valid ShortsUploadRequest request) {
        ShortsResponse response = shortsService.uploadShorts(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "숏폼 상세 조회", description = "특정 숏폼을 포함한 페이징 데이터를 조회합니다. (page=0, size=20)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숏폼을 찾을 수 없음")
    })
    @GetMapping("/{shortId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getShortsDetails(
            @Parameter(description = "숏폼 ID", example = "1") @PathVariable Long shortId,
            @Parameter(description = "페이지 정보 (기본: page=0, size=20)")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ShortsResponse> response = shortsService.getShortsDetailsWithPaging(shortId, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "숏폼 목록 조회", description = "숏폼 목록을 페이징하여 조회합니다. (스크롤링용: page=0&size=10)")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ShortsResponse>> getShortsList(
            @Parameter(description = "페이지 정보 (page, size, sort)")
            @PageableDefault(size = 8) Pageable pageable) {
        Page<ShortsResponse> response = shortsService.getShortsList(pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "숏폼 수정", description = "숏폼 정보를 수정합니다. (본인 영상만 수정 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숏폼을 찾을 수 없음")
    })
    @PatchMapping("/{shortId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShortsResponse> updateShorts(
            @Parameter(description = "숏폼 ID", example = "1") @PathVariable Long shortId,
            @RequestBody @Valid ShortsUpdateRequest request) {
        ShortsResponse response = shortsService.updateShorts(shortId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "숏폼 삭제", description = "숏폼을 삭제합니다. (본인 영상만 삭제 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숏폼을 찾을 수 없음")
    })
    @DeleteMapping("/{shortId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteShorts(
            @Parameter(description = "숏폼 ID", example = "1") @PathVariable Long shortId) {
        shortsService.deleteShorts(shortId);
        return ApiResponse.success();  // null 제거
    }
}

