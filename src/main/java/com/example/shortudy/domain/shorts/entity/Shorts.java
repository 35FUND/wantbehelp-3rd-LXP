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

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    @CreatedDate
    @Column(updatable = false)
    public LocalDateTime createdAt;

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

        // 기본값 설정
        this.status = status;
        this.shortsKeywords = new ArrayList<>();
    }

    /** Keyword 목록 조회 (편의 메서드)
     * ShortsKeyword를 거쳐서 실제 Keyword 엔티티 반환
     */
    public List<Keyword> getKeywords() {
        return shortsKeywords.stream()
                .map(ShortsKeyword::getKeyword)
                .collect(Collectors.toList());
    }


    // 비즈니스 메서드 (URL 수정)
    public void updateVideoUrl(String videoUrl) {
        if (videoUrl != null && !videoUrl.isBlank()) {
            validateUrl(videoUrl);
            this.videoUrl = videoUrl;
        }
    }

    // 비즈니스 메서드 (정보 수정)
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

    // 내부 검증 로직 - 제목
    private void validateTitle(String title) {
        if (title == null || title.isBlank() || title.length() > MAX_TITLE_LENGTH) {
            throw new BaseException(ErrorCode.SHORTS_TITLE_INVALID);
        }
    }

    // 내부 검증 로직 - URL
    private void validateUrl(String url) {
        if (url.length() > MAX_URL_LENGTH || !URL_PATTERN.matcher(url).matches()) {
            throw new BaseException(ErrorCode.SHORTS_URL_INVALID);
        }
    }
}