package com.example.shortudy.domain.shorts.entity;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.keyword.entity.Keyword;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shorts")
public class Shorts {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            Pattern.CASE_INSENSITIVE
    );

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_URL_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "video_url", length = MAX_URL_LENGTH)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = MAX_URL_LENGTH)
    private String thumbnailUrl;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ShortsStatus status;

    @OneToMany(mappedBy = "shorts", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShortsKeyword> shortsKeywords = new ArrayList<>();

    protected Shorts() {
    }

    public Shorts(User user, Category category, String title, String description,
                  String videoUrl, String thumbnailUrl, Integer durationSec, ShortsStatus status) {

        if (user == null || category == null) {
            throw new BaseException(ErrorCode.SHORTS_ESSENTIAL_INFO_MISSING);
        }

        validateTitle(title);
        if (videoUrl != null && !videoUrl.isBlank()) {
            validateUrl(videoUrl);
        }
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            validateUrl(thumbnailUrl);
        }
        this.user = user;
        this.category = category;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.durationSec = durationSec;
        this.status = status;
        this.shortsKeywords = new ArrayList<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public List<String> getKeywordNames() {
        return shortsKeywords.stream()
                .map(sk -> sk.getKeyword().getDisplayName())
                .collect(Collectors.toList());
    }

    public void addKeyword(Keyword keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException("Keyword는 null일 수 없습니다.");
        }

        boolean alreadyExists = shortsKeywords.stream()
                .anyMatch(sk -> sk.getKeyword().getId().equals(keyword.getId()));

        if (alreadyExists) {
            return;
        }

        ShortsKeyword shortsKeyword = ShortsKeyword.of(this, keyword);
        shortsKeywords.add(shortsKeyword);
    }

    public void addKeywords(List<Keyword> keywords) {
        if (keywords != null) {
            keywords.forEach(this::addKeyword);
        }
    }

    public void removeKeyword(Keyword keyword) {
        if (keyword == null) {
            return;
        }
        shortsKeywords.removeIf(sk ->
                sk.getKeyword().getId().equals(keyword.getId())
        );
    }

    public void clearKeywords() {
        shortsKeywords.clear();
    }

    public void replaceKeywords(List<Keyword> newKeywords) {
        clearKeywords();
        addKeywords(newKeywords);
    }


    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void updateVideoUrl(String videoUrl) {
        if (videoUrl != null && !videoUrl.isBlank()) {
            validateUrl(videoUrl);
            this.videoUrl = videoUrl;
        }
    }

    public void updateShorts(String title, String description, String thumbnailUrl,
                             Category category, ShortsStatus status) {
        if (title != null && !title.isBlank()) {
            validateTitle(title);
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            validateUrl(thumbnailUrl);
            this.thumbnailUrl = thumbnailUrl;
        }
        if (category != null) {
            this.category = category;
        }
        if (status != null) {
            this.status = status;
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank() || title.length() > MAX_TITLE_LENGTH) {
            throw new BaseException(ErrorCode.SHORTS_TITLE_INVALID);
        }
    }

    private void validateUrl(String url) {
        if (url.length() > MAX_URL_LENGTH || !URL_PATTERN.matcher(url).matches()) {
            throw new BaseException(ErrorCode.SHORTS_URL_INVALID);
        }
    }
}