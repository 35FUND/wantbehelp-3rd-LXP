package com.example.shortudy.domain.like.entity;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

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
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    protected ShortsLike() {}

    public static ShortsLike of(User user, Shorts shorts) {
        ShortsLike like = new ShortsLike();
        like.user = user;
        like.shorts = shorts;
        return like;
    }
}
