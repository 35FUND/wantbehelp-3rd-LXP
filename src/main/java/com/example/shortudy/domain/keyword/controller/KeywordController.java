package com.example.shortudy.domain.keyword.controller;

import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.domain.keyword.dto.request.KeywordRequest;
import com.example.shortudy.domain.keyword.dto.response.KeywordResponse;
import com.example.shortudy.domain.keyword.service.KeywordService;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/keywords")
public class KeywordController {

    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<KeywordResponse>>> getAllKeywords() {
        List<KeywordResponse> response = keywordService.getAllKeywords();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{keywordId}")
    public ResponseEntity<ApiResponse<KeywordResponse>> getKeyword(
            @PathVariable("keywordId") Long keywordId) {
        KeywordResponse response = keywordService.getKeyword(keywordId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{keywordId}")
    public ResponseEntity<ApiResponse<KeywordResponse>> updateKeyword(
            @PathVariable ("keywordId")Long keywordId,
            @RequestBody @Valid KeywordRequest request) {
        throw new BaseException(ErrorCode.NOT_IMPLEMENTED, "updateKeyword 메서드는 아직 구현되지 않았습니다.");
    }

    @DeleteMapping("/{keywordId}")
    public ResponseEntity<Void> deleteKeyword(
            @PathVariable ("keywordId") Long keywordId) {
            throw new BaseException(ErrorCode.NOT_IMPLEMENTED, "deleteKeyword 메서드는 아직 구현되지 않았습니다.");
    }
}
