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
 * [JSON 응답 예시]
 * {
 *   "id": 1,
 *   "title": "자바 학습 플레이리스트",
 *   "description": "자바 기초부터 심화까지",
 *   "visibility": "PUBLIC",
 *   "thumbnailUrl": "https://example.com/thumb.jpg",
 *   "shortsCount": 5,
 *   "owner": {
 *     "id": 10,
 *     "nickname": "홍길동",
 *     "profileUrl": "https://example.com/profile.jpg"
 *   },
 *   "createdAt": "2024-01-15T10:30:00",
 *   "updatedAt": "2024-01-20T14:00:00"
 * }
 */
public record PlaylistResponse(
        Long id,                        // 플레이리스트 ID
        String title,                   // 제목
        String description,             // 설명
        PlaylistVisibility visibility,  // 공개 여부
        String thumbnailUrl,            // 썸네일 이미지 URL
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
                playlist.getShortsCount(),
                new OwnerInfo(
                        playlist.getUser().getId(),
                        playlist.getUser().getNickname(),
                        playlist.getUser().getProfileUrl()
                ),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }

    /**
     * 플레이리스트 소유자 정보 (중첩 record)
     * [중첩(Nested) record란?]
     * - 외부 클래스 안에 정의된 record
     * - 관련된 데이터를 그룹화하여 구조적으로 표현
     * - 별도 파일 없이 간단하게 정의 가능
     */
    public record OwnerInfo(
            Long id,            // 소유자 ID
            String nickname,    // 닉네임
            String profileUrl   // 프로필 이미지 URL
    ) {
    }
}
