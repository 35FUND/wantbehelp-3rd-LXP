package com.example.shortudy.domain.comment.entity;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; // null이면 댓글, 값 있으면 대댓글

    @OneToMany(mappedBy = "parent")
    private List<Comment> children = new ArrayList<>(); // 댓글에 달린 대댓글 리스트

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shortsId", nullable = false)
    private Shorts shorts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(length = 1000, nullable = false)
    private String content;

    private LocalDateTime createdAt;

    protected Comment(){}

    public Comment(User user, Shorts shorts, Comment parent, String content) {
        this.user = user;
        this.shorts = shorts;
        this.parent = parent;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Comment getParent() {
        return parent;
    }

    public List<Comment> getChildren() {
        return children;
    }

    public Shorts getShorts() {
        return shorts;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
