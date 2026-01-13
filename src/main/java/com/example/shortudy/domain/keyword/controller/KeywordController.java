package com.example.shortudy.domain.keyword.controller;

import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.domain.keyword.dto.request.KeywordRequest;
import com.example.shortudy.domain.keyword.dto.response.KeywordResponse;
import com.example.shortudy.domain.keyword.service.KeywordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/keywords")
public class KeywordController {

    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KeywordResponse>> createKeyword(@RequestBody @Valid KeywordRequest request) {
        KeywordResponse response = keywordService.createKeyword(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
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
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "getKeyword 메서드는 아직 구현되지 않았습니다.");
    }

    @PutMapping("/{keywordId}")
    public ResponseEntity<KeywordResponse> updateKeyword(
            @PathVariable ("keywordId")Long keywordId,
            @RequestBody @Valid KeywordRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "updateKeyword 메서드는 아직 구현되지 않았습니다.");
    }

    @DeleteMapping("/{keywordId}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable ("keywordId") Long keywordId) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "deleteKeyword 메서드는 아직 구현되지 않았습니다.");
    }
}
