package com.example.shortudy.domain.comment.controller;

import com.example.shortudy.domain.comment.dto.request.CommentRequest;
import com.example.shortudy.domain.comment.dto.response.ReplyResponse;
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
public class ReplyController {

    private final CommentService commentService;

    public ReplyController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/comments/{parentId}/replies")
    public ResponseEntity<ApiResponse<Void>> createReply(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long parentId,
            @RequestBody CommentRequest request
    ) {

        commentService.createReply(me.getId(), parentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(null)
        );
    }

    @GetMapping("/comments/{parentId}/replies")
    public ResponseEntity<ApiResponse<List<ReplyResponse>>> getReplies(
            @PathVariable Long parentId,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long myId = (me != null) ? me.getId() : null;

        return ResponseEntity.ok(ApiResponse.success(commentService.findReplies(parentId, myId)));
    }

    @PatchMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponse<Void>> updateReply(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long replyId,
            @Valid @RequestBody CommentRequest request
    ) {
        // NOTE : 대댓글을 삭제하는 메서드 추가
        commentService.updateCommentReply(me.getId(), replyId, request);

        return ResponseEntity.ok(ApiResponse.success(null)
        );
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponse<Void>> deleteReply(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long replyId
    ) {
        // NOTE : 대댓글을 삭제하는 메서드 추가
        commentService.deleteCommentReply(me.getId(), replyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }
}
