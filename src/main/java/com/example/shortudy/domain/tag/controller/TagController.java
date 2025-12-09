package com.example.shortudy.domain.tag.controller;

import com.example.shortudy.domain.tag.dto.request.TagRequest;
import com.example.shortudy.domain.tag.dto.response.TagResponse;
import com.example.shortudy.domain.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tags", description = "태그 API")
@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Operation(summary = "태그 생성", description = "새로운 태그를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<TagResponse> createTag(@RequestBody TagRequest request) {
        TagResponse response = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "전체 태그 조회", description = "모든 태그 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        List<TagResponse> response = tagService.getAllTags();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "태그 단일 조회", description = "특정 태그를 조회합니다.")
    @GetMapping("/{tagId}")
    public ResponseEntity<TagResponse> getTag(
            @Parameter(description = "태그 ID", example = "1") @PathVariable Long tagId) {
        TagResponse response = tagService.getTag(tagId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "태그 수정", description = "태그를 수정합니다.")
    @PutMapping("/{tagId}")
    public ResponseEntity<TagResponse> updateTag(
            @Parameter(description = "태그 ID", example = "1") @PathVariable Long tagId,
            @RequestBody TagRequest request) {
        TagResponse response = tagService.updateTag(tagId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "태그 삭제", description = "태그를 삭제합니다.")
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "태그 ID", example = "1") @PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}
