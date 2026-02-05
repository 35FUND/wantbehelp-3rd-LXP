package com.example.shortudy.domain.like.entity;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(
        name = "shorts_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "shorts_id"})
)
public class ShortsLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorts_id", nullable = false)
    private Shorts shorts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ShortsLike() {}

    private ShortsLike(User user, Shorts shorts) {
        this.user = user;
        this.shorts = shorts;
    }

    public static ShortsLike of(User user, Shorts shorts) {
        Objects.requireNonNull(user, "유저 정보는 필수입니다");
        Objects.requireNonNull(shorts, "숏츠 정보는 필수입니다");

        return new ShortsLike(user, shorts);
    }
}
