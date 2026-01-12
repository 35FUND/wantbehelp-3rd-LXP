package com.example.shortudy.domain.comment.dto.response;

import com.example.shortudy.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(

        Long commentId,
        String content,
        LocalDateTime createdAt,
        WriterResponse writer,
        long replyCount,
        boolean isMine
) {

    public static CommentResponse from(Long myId, Comment comment, long replyCount) {

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                WriterResponse.from(comment.getUser()),
                replyCount,
                myId != null && comment.isWrittenBy(myId)
        );
    }
}

