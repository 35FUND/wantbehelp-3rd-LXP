package com.example.shortudy.domain.playlist.dto.response;

import com.example.shortudy.domain.keyword.entity.Keyword;
import com.example.shortudy.domain.playlist.entity.Playlist;
import com.example.shortudy.domain.playlist.entity.PlaylistShorts;
import com.example.shortudy.domain.playlist.entity.PlaylistVisibility;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 플레이리스트 상세 조회용 응답 DTO
 * [PlaylistResponse와의 차이점]
 * - PlaylistResponse: 목록 조회용 (숏츠 목록 미포함)
 * - PlaylistDetailResponse: 상세 조회용 (숏츠 목록 포함)
 * [용도]
 * - 플레이리스트 상세 조회 시 반환
 * - 숏츠 추가/삭제/순서변경 후 결과 반환
 */
public record PlaylistDetailResponse(
        Long id,                          // 플레이리스트 ID
        String title,                     // 제목
        String description,               // 설명
        PlaylistVisibility visibility,    // 공개 여부
        String thumbnailUrl,              // 썸네일 이미지 URL
        boolean thumbnailCustom,          // 사용자 지정 썸네일 여부 (false면 자동 썸네일)
        int shortsCount,                  // 담긴 숏츠 개수
        OwnerInfo owner,                  // 플레이리스트 소유자 정보
        List<PlaylistShortsItem> items,   // 담긴 숏츠 목록 (순서대로)
        LocalDateTime createdAt,          // 생성 일시
        LocalDateTime updatedAt           // 수정 일시
) {
    /**
     * 엔티티 → DTO 변환 메서드
     * [프론트 요청 반영]
     * - ShortsInfo에 videoUrl, description, status, category, keywords,
     *   viewCount, likeCount, commentCount, createdAt, updatedAt, isLiked 필드 추가
     * - commentCounts: 숏츠별 댓글 수 (배치 조회 결과)
     * - likedShortsIds: 현재 사용자가 좋아요한 숏츠 ID 목록
     *
     * @param playlist       변환할 Playlist 엔티티
     * @param commentCounts  숏츠 ID → 댓글 수 맵
     * @param likedShortsIds 현재 사용자가 좋아요한 숏츠 ID Set
     * @return 변환된 PlaylistDetailResponse DTO
     */
    public static PlaylistDetailResponse from(
            Playlist playlist,
            Map<Long, Long> commentCounts,
            Set<Long> likedShortsIds
    ) {
        List<PlaylistShortsItem> items = playlist.getPlaylistShorts().stream()
                .map(ps -> PlaylistShortsItem.from(ps, commentCounts, likedShortsIds))
                .toList();

        return new PlaylistDetailResponse(
                playlist.getId(),
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getVisibility(),
                playlist.getThumbnailUrl(),
                playlist.isThumbnailCustom(),
                playlist.getShortsCount(),
                OwnerInfo.from(playlist.getUser()),
                items,
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }

    /**
     * 플레이리스트에 담긴 아이템 정보
     * [필드 설명]
     * - itemId: PlaylistShorts의 고유 ID (아이템 식별자)
     * - position: 플레이리스트 내 순서 (0부터 시작)
     * - shorts: 숏츠 상세 정보 (중첩 객체)
     * - addedAt: 플레이리스트에 추가된 시간
     */
    public record PlaylistShortsItem(
            Long itemId,                // PlaylistShorts ID (아이템 고유 식별자)
            int position,               // 플레이리스트 내 순서
            ShortsInfo shorts,          // 숏츠 상세 정보
            LocalDateTime addedAt       // 플레이리스트에 추가된 시간
    ) {
        /**
         * PlaylistShorts 엔티티 → PlaylistShortsItem DTO 변환
         *
         * @param ps              변환할 PlaylistShorts 엔티티
         * @param commentCounts   숏츠 ID → 댓글 수 맵
         * @param likedShortsIds  현재 사용자가 좋아요한 숏츠 ID Set
         * @return 변환된 PlaylistShortsItem DTO
         */
        public static PlaylistShortsItem from(
                PlaylistShorts ps,
                Map<Long, Long> commentCounts,
                Set<Long> likedShortsIds
        ) {
            Shorts shorts = ps.getShorts();
            Long shortsId = shorts.getId();

            // 키워드 추출: ShortsKeyword → Keyword → displayName
            List<String> keywords = shorts.getShortsKeywords().stream()
                    .map(sk -> sk.getKeyword().getDisplayName())
                    .toList();

            // 카테고리 정보 (nullable)
            CategoryInfo categoryInfo = shorts.getCategory() != null
                    ? new CategoryInfo(shorts.getCategory().getId(), shorts.getCategory().getName())
                    : null;

            // 댓글 수 (맵에 없으면 0)
            long commentCount = commentCounts.getOrDefault(shortsId, 0L);

            // 좋아요 여부
            boolean isLiked = likedShortsIds.contains(shortsId);

            return new PlaylistShortsItem(
                    ps.getId(),
                    ps.getPosition(),
                    new ShortsInfo(
                            shortsId,
                            shorts.getTitle(),
                            shorts.getDescription(),
                            shorts.getVideoUrl(),
                            shorts.getThumbnailUrl(),
                            shorts.getDurationSec(),
                            shorts.getStatus(),
                            new UploaderInfo(
                                    shorts.getUser().getId(),
                                    shorts.getUser().getNickname(),
                                    shorts.getUser().getProfileUrl()
                            ),
                            categoryInfo,
                            keywords,
                            shorts.getViewCount(),
                            shorts.getLikeCount(),
                            commentCount,
                            shorts.getCreatedAt(),
                            shorts.getUpdatedAt(),
                            isLiked
                    ),
                    ps.getAddedAt()
            );
        }
    }

    /**
     * 숏츠 상세 정보 (아이템 내부 중첩 객체)
     * [승일님 요청으로 확장된 필드]
     * - videoUrl, description, status: 숏츠 기본 정보
     * - category: 카테고리 정보 (id, name)
     * - keywords: 키워드 목록
     * - viewCount, likeCount, commentCount: 통계 정보
     * - createdAt, updatedAt: 시간 정보
     * - isLiked: 현재 사용자의 좋아요 여부
     */
    public record ShortsInfo(
            Long shortsId,              // 숏츠 ID
            String title,               // 숏츠 제목
            String description,         // 숏츠 설명
            String videoUrl,            // 동영상 URL
            String thumbnailUrl,        // 숏츠 썸네일 URL
            Integer durationSec,        // 재생 시간(초)
            ShortsStatus status,        // 숏츠 상태
            UploaderInfo uploader,      // 숏츠 업로더 정보
            CategoryInfo category,      // 카테고리 정보 (nullable)
            List<String> keywords,      // 키워드 목록
            Long viewCount,             // 조회수
            Integer likeCount,          // 좋아요 수
            long commentCount,          // 댓글 수
            LocalDateTime createdAt,    // 생성 일시
            LocalDateTime updatedAt,    // 수정 일시
            boolean isLiked             // 현재 사용자 좋아요 여부
    ) {
    }

    /**
     * 숏츠 업로더 정보
     * - 플레이리스트 소유자(OwnerInfo)와 구분
     * - 숏츠를 업로드한 사람의 정보
     */
    public record UploaderInfo(
            Long id,            // 업로더 ID
            String nickname,    // 닉네임
            String profileUrl   // 프로필 이미지 URL
    ) {
    }

    /**
     * 카테고리 정보
     */
    public record CategoryInfo(
            Long id,            // 카테고리 ID
            String name         // 카테고리 이름
    ) {
    }
}
