package com.example.shortudy.domain.keyword.controller;

import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.domain.keyword.dto.response.KeywordResponse;
import com.example.shortudy.domain.keyword.service.KeywordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/keywords")
public class KeywordController {

    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KeywordResponse >>> search(
            @RequestParam(value ="q", required = false) String q) {
        List<KeywordResponse> response = keywordService.searchKeywords(q);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<KeywordResponse>>> getAllKeywords() {
        List<KeywordResponse> response = keywordService.getAllKeywords();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
