package com.example.shortudy.domain.shorts.entity;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "shorts_form")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ShortsStatus status = ShortsStatus.PUBLISHED;

    @OneToMany(mappedBy = "shorts")
    @Builder.Default
    private List<ShortsKeyword> shortsKeywords = new ArrayList<>();

    public void updateVideoUrl(String videoUrl) {
        if (videoUrl != null && !videoUrl.isBlank()) {
            validateUrl(videoUrl, "Video URL");
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
            validateUrl(thumbnailUrl, "Thumbnail URL");
            this.thumbnailUrl = thumbnailUrl;
        }
        if (category != null) {
            this.category = category;
        }
        if (status != null) {
            validateStatusTransition(status);
            this.status = status;
        }
    }

    private void validateTitle(String title) {
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Title must not exceed %d characters", MAX_TITLE_LENGTH)
            );
        }
    }

    private void validateUrl(String url, String fieldName) {
        if (url.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException(
                String.format("%s must not exceed %d characters", fieldName, MAX_URL_LENGTH)
            );
        }
        if (!URL_PATTERN.matcher(url).matches()) {
            throw new IllegalArgumentException(
                String.format("%s must be a valid URL format", fieldName)
            );
        }
    }

    private void validateStatusTransition(ShortsStatus newStatus) {
    }

    public boolean isPublished() {
        return ShortsStatus.PUBLISHED.equals(this.status);
    }

    public boolean hasValidVideoUrl() {
        return videoUrl != null && !videoUrl.isBlank() && URL_PATTERN.matcher(videoUrl).matches();
    }

    public boolean hasValidThumbnailUrl() {
        return thumbnailUrl != null && !thumbnailUrl.isBlank() && URL_PATTERN.matcher(thumbnailUrl).matches();
    }
}


