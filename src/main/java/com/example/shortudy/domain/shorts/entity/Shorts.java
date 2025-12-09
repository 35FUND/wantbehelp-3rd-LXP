package com.example.shortudy.domain.shorts.entity;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.tag.entity.Tag;
import com.example.shortudy.domain.tagging.Tagging;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "shorts_form")
public class Shorts extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "shorts", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tagging> taggings = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String videoUrl;

    private String thumbnailUrl;

    private Integer durationSec;

    @Enumerated(EnumType.STRING)
    private ShortsStatus shortsStatus;

    protected Shorts() {
    }

    // 태그 하나 추가
    public void addTag(Tag tag) {
        Tagging tagging = new Tagging(this, tag);
        this.taggings.add(tagging);
    }

    // 태그 전체 교체할 때 쓰기 좋음
    public void clearTags() {
        this.taggings.clear(); // orphanRemoval = true 라면 tagging row 삭제됨
    }

    public Shorts(User user, Category category, String title, String description, String videoUrl, String thumbnailUrl, Integer durationSec, ShortsStatus shortsStatus) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.durationSec = durationSec;
        this.shortsStatus = shortsStatus;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public List<Tagging> getTaggings() {
        return taggings;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public ShortsStatus getShortsStatus() {
        return shortsStatus;
    }

    public void updateShorts(String title, String description, String thumbnailUrl, Category category, ShortsStatus status) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            this.thumbnailUrl = thumbnailUrl;
        }
        if (category != null) {
            this.category = category;
        }
        if (status != null) {
            this.shortsStatus = status;
        }
    }
}


