package com.example.shortudy.domain.shorts.entity;

import com.example.shortudy.domain.keyword.entity.Keyword;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"shorts_id", "keyword_id"})
)
public class ShortsKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorts_id", nullable = false)
    private Shorts shorts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    protected ShortsKeyword() {}

    private ShortsKeyword(Shorts shorts, Keyword keyword) {
        this.shorts = shorts;
        this.keyword = keyword;
    }

    public static ShortsKeyword of(Shorts shorts, Keyword keyword) {
        return new ShortsKeyword(shorts, keyword);
    }

    public void setShorts(Shorts shorts) {
        this.shorts = shorts;
    }
}