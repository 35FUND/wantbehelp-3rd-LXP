package com.example.shortudy.domain.keyword.controller;

import com.example.shortudy.domain.keyword.dto.request.KeywordRequest;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.domain.keyword.dto.response.KeywordResponse;
import com.example.shortudy.domain.keyword.service.KeywordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KeywordResponse>> getById(@PathVariable Long id) {
        KeywordResponse response = keywordService.getKeyword(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KeywordResponse>>> search(
            @RequestParam(value = "q", required = false) String q) {
        List<KeywordResponse> response = keywordService.searchKeywords(q);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KeywordResponse>> create(@RequestBody KeywordRequest req) {
        // [수정] req.displayName() 대신 req.name() 사용 (KeywordRequest 레코드 규격)
        KeywordResponse created = keywordService.createKeyword(req.name());
        return ResponseEntity.created(URI.create("/api/v1/keywords/" + created.id()))
                .body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KeywordResponse>> update(
            @PathVariable Long id,
            @RequestBody KeywordRequest req) {
        // [수정] req.displayName() 대신 req.name() 사용
        KeywordResponse updated = keywordService.updateKeyword(id, req.name());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        keywordService.deleteKeyword(id);
        return ResponseEntity.noContent().build();
    }
}
