package com.example.shortudy.domain.shorts.entity;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Entity
@Getter
@Table(name = "shorts_form")
public class Shorts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ShortsStatus status = ShortsStatus.PUBLISHED;

    @OneToMany(mappedBy = "shorts")
    private List<ShortsKeyword> shortsKeywords = new ArrayList<>();

    protected Shorts() {
    }

    public Shorts(User user, Category category, String title, String description, String videoUrl, String thumbnailUrl, Integer durationSec, ShortsStatus status) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.durationSec = durationSec;
        this.status = status;
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

    public ShortsStatus getStatus() {
        return status;
    }

    public void updateVideoUrl(String videoUrl) {
        if (videoUrl != null && !videoUrl.isBlank()) {
            this.videoUrl = videoUrl;
        }
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
            this.status = status;
        }
    }
}


