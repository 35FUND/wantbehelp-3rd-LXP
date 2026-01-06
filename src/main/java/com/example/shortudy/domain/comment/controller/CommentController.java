package com.example.shortudy.domain.comment.controller;

import com.example.shortudy.domain.comment.dto.request.CommentRequest;
import com.example.shortudy.domain.comment.dto.response.CommentResponse;
import com.example.shortudy.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vi")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/shorts/{shortsId}/comments")
    public ResponseEntity<CommentResponse> createComments(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long shortsId,
            @Valid @RequestBody CommentRequest commentRequest
    ) {
        return ResponseEntity.ok(commentService.createComment(me.getUserId(), shortsId, commentRequest));
    }

    @GetMapping("shorts/{shortsId}/comments")
    public ResponseEntity<List<CommentResponse>> getAllComments(
            @PathVariable Long shortsId
    ) {
       return
    }
}
