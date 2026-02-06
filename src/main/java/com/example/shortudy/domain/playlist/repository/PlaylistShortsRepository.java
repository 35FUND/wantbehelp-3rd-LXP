package com.example.shortudy.domain.playlist.repository;

import com.example.shortudy.domain.playlist.entity.PlaylistShorts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;

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

    // ========== 벌크 연산 (position 일괄 업데이트) ==========

    /**
     * 삭제 후 position 재정렬 — 벌크 연산
     * 삭제된 위치(removedPosition) 뒤의 항목들의 position을 1씩 감소
     * [기존 방식 대비 개선점]
     * - 기존: N개 항목 각각 dirty checking → N번 UPDATE
     * - 개선: 1번의 벌크 UPDATE로 처리
     *
     * @param playlistId      대상 플레이리스트 ID
     * @param removedPosition 삭제된 항목의 position
     * @return 업데이트된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlaylistShorts ps SET ps.position = ps.position - 1 " +
            "WHERE ps.playlist.id = :playlistId AND ps.position > :removedPosition")
    int bulkDecrementPositionAfter(@Param("playlistId") Long playlistId,
                                   @Param("removedPosition") int removedPosition);

    /**
     * 순서 변경 시 아래로 이동 — 벌크 연산
     * 아이템을 아래(뒤)로 이동할 때, 사이 항목들의 position을 1씩 감소
     * 예: [A:0, B:1, C:2, D:3] 에서 B(1)를 3으로 이동
     *     → C(2→1), D(3→2) position -1 처리
     *
     * @param playlistId   대상 플레이리스트 ID
     * @param oldPosition  이동 대상의 현재 position (exclusive)
     * @param newPosition  이동 대상의 새 position (inclusive)
     * @return 업데이트된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlaylistShorts ps SET ps.position = ps.position - 1 " +
            "WHERE ps.playlist.id = :playlistId " +
            "AND ps.position > :oldPosition AND ps.position <= :newPosition")
    int bulkDecrementPositionBetween(@Param("playlistId") Long playlistId,
                                     @Param("oldPosition") int oldPosition,
                                     @Param("newPosition") int newPosition);

    /**
     * 순서 변경 시 위로 이동 — 벌크 연산
     * 아이템을 위(앞)로 이동할 때, 사이 항목들의 position을 1씩 증가
     * 예: [A:0, B:1, C:2, D:3] 에서 D(3)를 1로 이동
     *     → B(1→2), C(2→3) position +1 처리
     *
     * @param playlistId   대상 플레이리스트 ID
     * @param newPosition  이동 대상의 새 position (inclusive)
     * @param oldPosition  이동 대상의 현재 position (exclusive)
     * @return 업데이트된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlaylistShorts ps SET ps.position = ps.position + 1 " +
            "WHERE ps.playlist.id = :playlistId " +
            "AND ps.position >= :newPosition AND ps.position < :oldPosition")
    int bulkIncrementPositionBetween(@Param("playlistId") Long playlistId,
                                     @Param("newPosition") int newPosition,
                                     @Param("oldPosition") int oldPosition);

    /**
     * 특정 아이템의 position을 직접 변경 — 벌크 연산
     *
     * @param playlistId  대상 플레이리스트 ID
     * @param shortsId    대상 숏츠 ID
     * @param newPosition 새로운 position
     * @return 업데이트된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlaylistShorts ps SET ps.position = :newPosition " +
            "WHERE ps.playlist.id = :playlistId AND ps.shorts.id = :shortsId")
    int bulkUpdatePosition(@Param("playlistId") Long playlistId,
                           @Param("shortsId") Long shortsId,
                           @Param("newPosition") int newPosition);
}
