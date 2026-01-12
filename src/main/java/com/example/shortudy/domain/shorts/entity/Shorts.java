package com.example.shortudy.domain.shorts.entity;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.keyword.entity.Keyword;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Shorts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @OneToMany(mappedBy = "shorts", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShortsKeyword> shortsKeywords = new ArrayList<>();

    // ... 다른 필드들

    protected Shorts() {}

    private Shorts(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public static Shorts of(String title, String content) {
        return new Shorts(title, content);
    }

    // 비즈니스 로직
    public void addKeyword(Keyword keyword) {
        ShortsKeyword shortsKeyword = ShortsKeyword.of(this, keyword);
        shortsKeyword.setShorts(this);
        this.shortsKeywords.add(shortsKeyword);
    }

    public void removeKeyword(Keyword keyword) {
        this.shortsKeywords.removeIf(
                sk -> sk.getKeyword().getId().equals(keyword.getId())
        );
    }

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateShorts(@Size(max = 100, message = "제목은 100자 이하여야 합니다.") String title, String description, String s, Category category, ShortsStatus status) {
    }


    public String getThumbnailUrl() {
        return null;
    }
}