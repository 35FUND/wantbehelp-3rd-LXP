package com.example.shortudy.domain.playlist.dto.response;

import com.example.shortudy.domain.playlist.entity.Playlist;
import com.example.shortudy.domain.playlist.entity.PlaylistVisibility;

import java.time.LocalDateTime;

/**
 * 플레이리스트 목록 조회용 응답 DTO
 * [용도]
 * - 플레이리스트 목록 조회 시 반환 (내 플레이리스트, 공개 플레이리스트 등)
 * - 플레이리스트 생성/수정 후 결과 반환
 * - 상세 조회가 아니므로 담긴 숏츠 목록은 포함하지 않음
 */
public record PlaylistResponse(
        Long id,                        // 플레이리스트 ID
        String title,                   // 제목
        String description,             // 설명
        PlaylistVisibility visibility,  // 공개 여부
        String thumbnailUrl,            // 썸네일 이미지 URL
        boolean thumbnailCustom,        // 사용자 지정 썸네일 여부 (false면 자동 썸네일)
        int shortsCount,                // 담긴 숏츠 개수
        OwnerInfo owner,                // 소유자 정보
        LocalDateTime createdAt,        // 생성 일시
        LocalDateTime updatedAt         // 수정 일시
) {
    /**
     * 엔티티 → DTO 변환 메서드 (정적 팩토리 메서드)
     * [from() 메서드 패턴]
     * - "~로부터 만든다"는 의미
     * - 엔티티를 받아서 DTO로 변환
     * - 변환 로직을 한 곳에서 관리 → 유지보수 용이
     * [사용 예시]
     * Playlist entity = playlistRepository.findById(1L);
     * PlaylistResponse dto = PlaylistResponse.from(entity);
     * @param playlist 변환할 Playlist 엔티티
     * @return 변환된 PlaylistResponse DTO
     */
    public static PlaylistResponse from(Playlist playlist) {
        return new PlaylistResponse(
                playlist.getId(),
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getVisibility(),
                playlist.getThumbnailUrl(),
                playlist.isThumbnailCustom(),
                playlist.getShortsCount(),
                OwnerInfo.from(playlist.getUser()),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }
}
