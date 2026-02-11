package com.example.shortudy.domain.comment.controller;

import com.example.shortudy.domain.comment.dto.request.CommentRequest;
import com.example.shortudy.domain.comment.dto.response.CommentListResponse;
import com.example.shortudy.domain.comment.dto.response.CommentResponse;
import com.example.shortudy.domain.comment.service.CommentService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 생성
    @PostMapping("/shorts/{shortsId}/comments")
    public ResponseEntity<ApiResponse<Void>> createComments(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long shortsId,
            @Valid @RequestBody CommentRequest request
    ) {
        commentService.createComment(me.getId(), shortsId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(null)
        );
    }

    @GetMapping("/shorts/{shortsId}/comments")
    public ResponseEntity<ApiResponse<CommentListResponse>> getComments(
            @PathVariable Long shortsId,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long myId = (me != null) ? me.getId() : null;

        return ResponseEntity.ok(ApiResponse.success(commentService.findComments(shortsId, myId)));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request
    ) {
        commentService.updateComment(me.getId(), commentId, request);

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(me.getId(), commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }

    // 댓글 신고
    // - 로그인한 사용자들만 신고할 수 있어야 한다.
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<Void>> reportComment(
        @AuthenticationPrincipal CustomUserDetails me,
        @PathVariable Long commentId
    ) {
        commentService.reportComment(me.getId(), commentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }
}
