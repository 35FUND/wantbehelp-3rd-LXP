package com.example.shortudy.domain.playlist.repository;

import com.example.shortudy.domain.playlist.entity.PlaylistShorts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 플레이리스트-숏츠 연결 테이블 Repository
 * [이 Repository의 역할]
 * - PlaylistShorts 엔티티(중간 테이블)에 대한 DB 작업
 * - 플레이리스트와 숏츠 간의 관계 데이터 관리
 * [중간 테이블 구조]
 * Playlist (1) ──┬── (*) PlaylistShorts (*) ──┬── (1) Shorts
 *                │                            │
 *          playlist_id                    shorts_id
 *                       + position (순서)
 *                       + added_at (추가일시)
 */
@Repository
public interface PlaylistShortsRepository extends JpaRepository<PlaylistShorts, Long> {

    Optional<PlaylistShorts> findByPlaylistIdAndShortsId(Long playlistId, Long shortsId);

    boolean existsByPlaylistIdAndShortsId(Long playlistId, Long shortsId);

    /**
     * 플레이리스트 내 최대 순서 번호 조회
     * [COALESCE 함수]
     * - COALESCE(값, 기본값): 값이 null이면 기본값 반환
     * - 숏츠가 하나도 없으면 MAX()가 null을 반환하므로 -1로 대체
     * - 다음 순서 = 최대 순서 + 1 이므로, 빈 플레이리스트의 첫 숏츠는 0번
     * [사용 목적]
     * - 새 숏츠 추가 시 다음 순서 번호 계산
     * - 예: 현재 최대가 2면 → 다음 숏츠는 3번
     */
    @Query("SELECT COALESCE(MAX(ps.position), -1) FROM PlaylistShorts ps " +
            "WHERE ps.playlist.id = :playlistId")
    int findMaxPositionByPlaylistId(@Param("playlistId") Long playlistId);

    /**
     * 플레이리스트의 숏츠 목록 조회 (페이지네이션 + fetch join)
     * [조회 결과 구조]
     * PlaylistShorts
     *   └─ shorts (Shorts)
     *        └─ user (User) - 숏츠 작성자
     * [주의사항]
     * - fetch join과 Pageable을 함께 사용하면 Hibernate가 경고를 발생시킬 수 있음
     * - "HHH90003004: firstResult/maxResults specified with collection fetch"
     * - 이 경우 메모리에서 페이징이 이루어져 데이터가 많으면 성능 저하 가능
     */
    @Query("SELECT ps FROM PlaylistShorts ps " +
            "JOIN FETCH ps.shorts s " +
            "JOIN FETCH s.user " +
            "WHERE ps.playlist.id = :playlistId")

    Page<PlaylistShorts> findByPlaylistIdWithShorts(@Param("playlistId") Long playlistId, Pageable pageable);

    void deleteByShortsId(Long shortsId);
}
