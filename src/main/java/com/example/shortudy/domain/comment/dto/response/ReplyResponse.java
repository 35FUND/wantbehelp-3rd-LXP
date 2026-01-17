package com.example.shortudy.domain.comment.dto.response;

import com.example.shortudy.domain.comment.entity.Comment;
import com.example.shortudy.domain.comment.entity.CommentStatus;

import java.time.LocalDateTime;

public record ReplyResponse(
        Long replyId,
        Long parentId,          // 어떤 댓글의 reply인지(부모 id)
        String content,
        LocalDateTime createdAt,
        WriterResponse writer,
        boolean isMine
) {

    public static ReplyResponse from(Comment reply, Long myId) {

        return new ReplyResponse(
                reply.getId(),
                reply.getParent().getId(),
                reply.getStatus() == CommentStatus.DELETED ? "삭제된 댓글입니다." : reply.getContent(),
                reply.getCreatedAt(),
                WriterResponse.from(reply.getUser()),
                myId != null && reply.isWrittenBy(myId)
        );

    }
}
