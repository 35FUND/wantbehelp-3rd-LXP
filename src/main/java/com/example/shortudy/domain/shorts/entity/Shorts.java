package com.example.shortudy.domain.shorts.entity;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.keyword.entity.Keyword;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
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

    /** Keyword 목록 조회
     * ShortsKeyword 연관 엔티티에서 실제 Keyword 엔티티를 추출합니다.
     */
    public List<Keyword> getKeywords() {
        return shortsKeywords.stream()
                .map(ShortsKeyword::getKeyword)
                .collect(Collectors.toList());
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void updateVideoUrl(String videoUrl) {
        if (videoUrl != null && !videoUrl.isBlank()) {
            validateUrl(videoUrl);
            this.videoUrl = videoUrl;
        }
    }

    public void updateShorts(String title, String description, String thumbnailUrl,
                             Category category, Integer durationSec, ShortsStatus status) {
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
        if (durationSec != null) {
            updateDurationSec(durationSec);
        }
        if (status != null) {
            changeStatus(status);
        }
    }

    public void updateDurationSec(Integer durationSec) {
        if (durationSec == null || durationSec <= 0) {
            throw new BaseException(ErrorCode.SHORTS_DURATION_INVALID);
        }
        this.durationSec = durationSec;
    }

    public void changeStatus(ShortsStatus nextStatus) {
        if (nextStatus == null || this.status == nextStatus) {
            return;
        }
        if (!isValidStatusTransition(this.status, nextStatus)) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "허용되지 않는 숏츠 상태 전이입니다.");
        }
        this.status = nextStatus;
    }

    private boolean isValidStatusTransition(ShortsStatus currentStatus, ShortsStatus nextStatus) {
        if (currentStatus == null) {
            return true;
        }
        return switch (currentStatus) {
            case PENDING -> nextStatus == ShortsStatus.AI_CHECK
                    || nextStatus == ShortsStatus.PUBLISHED
                    || nextStatus == ShortsStatus.REJECT;
            case AI_CHECK -> nextStatus == ShortsStatus.PUBLISHED
                    || nextStatus == ShortsStatus.REJECT;
            case PUBLISHED, REJECT -> false;
        };
    }

    public void addKeyword(Keyword keyword) {
        ShortsKeyword shortsKeyword = ShortsKeyword.of(this, keyword);
        this.shortsKeywords.add(shortsKeyword);
    }

    public void clearKeywords() {
        this.shortsKeywords.clear();
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

    public boolean isWrittenBy(Long userId) {

        return this.user.getId().equals(userId);
    }

    // 업로드 미완료 세션 정리 시, 고아 숏츠로 삭제 가능한지 판단한다.
    public boolean canBeDeletedAsUploadOrphan() {
        return this.status == ShortsStatus.PENDING;
    }
}
