package com.example.shortudy.domain.comment.entity;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
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

    private LocalDateTime createdAt;

    private static final int MAX_CONTENT_LENGTH = 1000;

    protected Comment(){}

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
}
