package com.example.shortudy.domain.comment.dto.response;

import com.example.shortudy.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse (

        Long commentId,
        Long parentId,
        String content,
        LocalDateTime createdAt
){

    public static CommentResponse from(Comment comment){

        return new CommentResponse(
                comment.getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
