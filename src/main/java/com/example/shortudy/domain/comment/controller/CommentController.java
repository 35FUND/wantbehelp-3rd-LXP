package com.example.shortudy.domain.comment.controller;

import com.example.shortudy.domain.comment.dto.request.CommentRequest;
import com.example.shortudy.domain.comment.dto.response.CommentResponse;
import com.example.shortudy.domain.comment.dto.response.ReplyResponse;
import com.example.shortudy.domain.comment.service.CommentService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<CommentResponse>> createComments(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long shortsId,
            @Valid @RequestBody CommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(commentService.createComment(me.getId(), shortsId, request))
                );
    }

    @GetMapping("/shorts/{shortsId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long shortsId,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long myId = (me != null) ? me.getId() : null;

        // List<CommentResponse> foundComments = commentService.findComments(shortsId, myId);
        return ResponseEntity.ok(ApiResponse.success(commentService.findComments(shortsId, myId)));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(commentService.updateComment(me.getId(), commentId, request))
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
}
