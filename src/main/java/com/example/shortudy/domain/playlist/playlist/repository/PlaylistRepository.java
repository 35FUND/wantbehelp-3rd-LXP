package com.example.shortudy.domain.playlist.playlist.repository;

import com.example.shortudy.domain.playlist.entity.Playlist;
import com.example.shortudy.domain.playlist.entity.PlaylistVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    @Query("SELECT p FROM Playlist p " +
            "JOIN FETCH p.user " +
            "WHERE p.id = :id")
    Optional<Playlist> findWithUserById(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Playlist p " +
            "JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.playlistShorts ps " +
            "LEFT JOIN FETCH ps.shorts s " +
            "LEFT JOIN FETCH s.user " +
            "WHERE p.id = :id")
    Optional<Playlist> findWithDetailsById(@Param("id") Long id);

    /**
     * 특정 사용자의 플레이리스트 목록 조회 (페이지네이션)
     * [메서드 이름 쿼리]
     * - findByUserId → "WHERE user.id = ?"
     * - Spring Data JPA가 메서드 이름을 분석해서 쿼리 생성
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Playlist> findByUserId(Long userId, Pageable pageable);

    /**
     * 특정 사용자의 특정 공개범위 플레이리스트 목록 조회
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Playlist> findByUserIdAndVisibility(
            Long userId,
            PlaylistVisibility visibility,
            Pageable pageable
    );

    /**
     * 공개범위별 플레이리스트 목록 조회
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Playlist> findByVisibility(PlaylistVisibility visibility, Pageable pageable);

    /**
     * 제목으로 플레이리스트 검색 (특정 공개범위)
     * [LIKE 검색]
     * - CONCAT('%', :query, '%'): query를 포함하는 모든 문자열
     * - 예: query가 "자바"면 "자바 기초", "고급 자바", "자바" 모두 매칭
     * [countQuery 분리]
     * - 메인 쿼리에 JOIN FETCH가 포함되어 있으므로 count 쿼리를 분리
     * - count 쿼리에서 불필요한 fetch join을 제거하여 성능 개선
     * [참고] 대규모 데이터에서 LIKE '%keyword%'는 인덱스를 타지 못함
     * - 추후 MySQL Full-Text Index 도입 시 MATCH AGAINST로 전환 권장
     */
    @Query(value = "SELECT p FROM Playlist p " +
            "JOIN FETCH p.user " +
            "WHERE p.visibility = :visibility " +
            "AND p.title LIKE CONCAT('%', :query, '%')",
            countQuery = "SELECT COUNT(p) FROM Playlist p " +
                    "WHERE p.visibility = :visibility " +
                    "AND p.title LIKE CONCAT('%', :query, '%')")
    Page<Playlist> searchByTitleAndVisibility(
            @Param("query") String query,
            @Param("visibility") PlaylistVisibility visibility,
            Pageable pageable
    );

    /**
     * 특정 사용자의 플레이리스트 개수 조회
     * [countBy... 메서드]
     * - SELECT COUNT(*) 쿼리 생성
     * - 확장성 고려
     */
    long countByUserId(Long userId);

    /**
     * 특정 숏츠가 담긴 플레이리스트 개수 조회
     * - 숏츠가 얼마나 많은 플레이리스트에 담겨있는지 확인
     * - COUNT(DISTINCT p): 중복 제거 후 개수
     */
    @Query("SELECT COUNT(DISTINCT p) FROM Playlist p " +
            "JOIN p.playlistShorts ps " +
            "WHERE ps.shorts.id = :shortsId")
    long countByContainingShortsId(@Param("shortsId") Long shortsId);
}
