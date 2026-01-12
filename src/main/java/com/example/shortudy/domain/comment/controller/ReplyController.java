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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReplyController {

    private final CommentService commentService;

    public ReplyController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/comments/{parentId}/replies")
    public ResponseEntity<ApiResponse<ReplyResponse>> createReply(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long parentId,
            @RequestBody CommentRequest request
            ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(commentService.createReply(me.getId(), parentId, request))
        );
    }

    @GetMapping("/comments/{parentId}/replies")
    public ResponseEntity<ApiResponse<List<ReplyResponse>>> getReplies(
            @PathVariable Long parentId,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long myId = (me != null) ? me.getId() : null;

        // List<ReplyResponse> foundReplies = commentService.findReplies(parentId, myId);

        return ResponseEntity.ok(ApiResponse.success(commentService.findReplies(parentId, myId)));
    }
