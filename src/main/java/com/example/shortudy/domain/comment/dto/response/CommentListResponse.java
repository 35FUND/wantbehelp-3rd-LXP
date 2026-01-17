package com.example.shortudy.domain.comment.dto.response;

import java.util.List;

public record CommentListResponse(
    long totalCommentCount,
    List<CommentResponse> comments
) {
}