package com.example.shortudy.domain.comment.dto.response;

import com.example.shortudy.domain.comment.entity.Comment;
import com.example.shortudy.domain.comment.entity.CommentStatus;

import java.time.LocalDateTime;

public record CommentResponse(

        Long shortsId,
        Long commentId,
        String content,
        LocalDateTime createdAt,
        WriterResponse writer,
        long replyCount,
        boolean isMine,
        boolean isReported
) {

    public static CommentResponse from(Long myId, Comment comment, long replyCount, boolean isReported) {

        return new CommentResponse(
                comment.getShorts().getId(),
                comment.getId(),
                comment.getStatus() == CommentStatus.DELETED ? "삭제된 댓글입니다." : comment.getContent(),
                comment.getCreatedAt(),
                WriterResponse.from(comment.getUser()),
                replyCount,
                myId != null && comment.isWrittenBy(myId),
                isReported
        );
    }
}

