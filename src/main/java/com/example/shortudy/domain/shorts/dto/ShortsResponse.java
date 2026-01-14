package com.example.shortudy.domain.shorts.dto;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;

/**
 * 숏폼 응답 DTO
 * 
 * <p>숏폼 비디오 정보를 포함하는 플랫 구조의 DTO 레코드입니다.
 * 업로더와 카테고리 정보를 중첩 객체 대신 플랫 필드로 제공하여
 * 직렬화 복잡성을 줄이고 확장성을 향상시켰습니다.</p>
 * 
 * @param shortsId        숏폼 고유 ID
 * @param title           숏폼 제목
 * @param description     숏폼 설명
 * @param videoUrl        비디오 URL
 * @param thumbnailUrl    썸네일 URL
 * @param durationSec     비디오 재생 시간 (초)
 * @param status          숏폼 상태 (PUBLISHED, DRAFT, etc.)
 * @param userId          업로더 사용자 ID
 * @param userNickname    업로더 닉네임
 * @param userProfileUrl  업로더 프로필 이미지 URL
 * @param categoryId      카테고리 ID
 * @param categoryName    카테고리 이름
 */
public record ShortsResponse(
        Long shortsId,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        Integer durationSec,
        ShortsStatus status,
        Long userId,
        String userNickname,
        String userProfileUrl,
        Long categoryId,
        String categoryName
) {

    /**
     * Shorts 엔티티를 ShortsResponse DTO로 변환합니다.
     * 
     * <p>이 메서드는 엔티티의 관계 정보를 플랫 구조의 DTO 필드로 매핑합니다.
     * User와 Category 엔티티의 정보를 각각 해당 필드에 직접 매핑하여
     * 중첩 구조 없이 직렬화할 수 있습니다.</p>
     * 
     * @param shorts 변환할 Shorts 엔티티
     * @return 변환된 ShortsResponse DTO
     * @throws IllegalArgumentException shorts나 관련 엔티티가 null인 경우
     */
    public static ShortsResponse from(Shorts shorts) {
        if (shorts == null) {
            throw new IllegalArgumentException("Shorts entity cannot be null");
        }
        if (shorts.getUser() == null) {
            throw new IllegalArgumentException("Shorts user cannot be null");
        }
        if (shorts.getCategory() == null) {
            throw new IllegalArgumentException("Shorts category cannot be null");
        }

        return new ShortsResponse(
                shorts.getId(),
                shorts.getTitle(),
                shorts.getDescription(),
                shorts.getVideoUrl(),
                shorts.getThumbnailUrl(),
                shorts.getDurationSec(),
                shorts.getStatus(),
                shorts.getUser().getId(),
                shorts.getUser().getNickname(),
                shorts.getUser().getProfileUrl(),
                shorts.getCategory().getId(),
                shorts.getCategory().getName()
        );
    }
}




