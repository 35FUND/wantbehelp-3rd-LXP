package com.example.shortudy.domain.playlist.playlist.dto.response;

import com.example.shortudy.domain.playlist.entity.Playlist;
import com.example.shortudy.domain.playlist.entity.PlaylistShorts;
import com.example.shortudy.domain.playlist.entity.PlaylistVisibility;

import java.time.LocalDateTime;
import java.util.List;

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
     * [Stream API 사용]
     * - playlist.getPlaylistShorts(): 플레이리스트에 담긴 숏츠 목록 (List)
     * - .stream(): List를 Stream으로 변환
     * - .map(PlaylistShortsItem::from): 각 항목을 DTO로 변환
     * - .toList(): 다시 List로 변환
     *
     * @param playlist 변환할 Playlist 엔티티
     * @return 변환된 PlaylistDetailResponse DTO
     */
    public static PlaylistDetailResponse from(Playlist playlist) {
        // PlaylistShorts 엔티티 목록 → PlaylistShortsItem DTO 목록으로 변환
        List<PlaylistShortsItem> items = playlist.getPlaylistShorts().stream()
                .map(PlaylistShortsItem::from)  // 각 항목을 DTO로 변환
                .toList();                       // List로 수집

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
         * @param ps 변환할 PlaylistShorts 엔티티
         * @return 변환된 PlaylistShortsItem DTO
         */
        public static PlaylistShortsItem from(PlaylistShorts ps) {
            return new PlaylistShortsItem(
                    ps.getId(),                          // PlaylistShorts의 ID (아이템 ID)
                    ps.getPosition(),                    // 이 플레이리스트에서의 순서
                    new ShortsInfo(
                            ps.getShorts().getId(),
                            ps.getShorts().getTitle(),
                            ps.getShorts().getThumbnailUrl(),
                            ps.getShorts().getDurationSec(),
                            new UploaderInfo(
                                    ps.getShorts().getUser().getId(),
                                    ps.getShorts().getUser().getNickname()
                            )
                    ),
                    ps.getAddedAt()                      // 플레이리스트에 추가된 시간
            );
        }
    }

    /**
     * 숏츠 상세 정보 (아이템 내부 중첩 객체)
     * - shortsId: 숏츠 고유 ID
     * - title: 숏츠 제목
     * - thumbnailUrl: 숏츠 썸네일
     * - durationSec: 재생 시간(초)
     * - uploader: 숏츠 업로더 정보
     */
    public record ShortsInfo(
            Long shortsId,              // 숏츠 ID
            String title,               // 숏츠 제목
            String thumbnailUrl,        // 숏츠 썸네일 URL
            Integer durationSec,        // 재생 시간(초)
            UploaderInfo uploader       // 숏츠 업로더 정보
    ) {
    }

    /**
     * 숏츠 업로더 정보
     * - 플레이리스트 소유자(OwnerInfo)와 구분
     * - 숏츠를 업로드한 사람의 정보
     */
    public record UploaderInfo(
            Long id,            // 업로더 ID
            String nickname     // 닉네임
    ) {
    }
}
