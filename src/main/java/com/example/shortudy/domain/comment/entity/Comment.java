package com.example.shortudy.domain.comment.entity;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; // null이면 댓글, 값 있으면 대댓글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shortsId", nullable = false)
    private Shorts shorts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(length = 1000, nullable = false)
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.ACTIVE;

    private LocalDateTime deletedAt;

    private static final int MAX_CONTENT_LENGTH = 1000;

    protected Comment() {
    }

    private Comment(User user, Shorts shorts, Comment parent, String content) {
        this.user = user;
        this.shorts = shorts;
        this.parent = parent;
        validateContent(content);
        this.content = content;
    }

    // 댓글
    public static Comment create(User user, Shorts shorts, String content) {
        return new Comment(user, shorts, null, content);
    }

    public void updateContent(String content) {

        validateContent(content);
        this.content = content;
    }


    public boolean isWrittenBy(Long userId) {

        return this.user.getId().equals(userId);
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }
    }

    // 대댓글
    public static Comment reply(User user, Comment parent, String content) {

        // Comment parent가 대댓글인 경우 예외 처리
        if (parent.getParent() != null) {
            throw new BaseException(ErrorCode.COMMENT_NOT_FOUND);
        }

        return new Comment(user, parent.getShorts(), parent, content);
    }

    public void softDelete(Long userId) {

        if((!isWrittenBy(userId))) {
            throw new BaseException(ErrorCode.COMMENT_FORBIDDEN);
        }

        // NOTE : 멱등성을 위해서 return을 해줘도 되긴 함 !
        if (this.status == CommentStatus.DELETED) {
            return; // 이미 삭제된 경우 아무 작업도 수행하지 않음
        }

        this.status = CommentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}
