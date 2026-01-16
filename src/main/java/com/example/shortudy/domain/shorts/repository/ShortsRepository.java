package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortsRepository extends JpaRepository<Shorts, Long> {

    /**
     * 조회수 일괄 업데이트 (Redis -> DB)
     */
    @Modifying
    @Query("UPDATE Shorts s SET s.viewCount = s.viewCount + :count WHERE s.id = :id")
    void updateViewCount(@Param("id") Long id, @Param("count") Long count);


    // ============================================
    // 기본 조회
    // ============================================

    /**
     * 상세 조회 - 기본 정보 (User, Category)
     */
    @Query("SELECT s FROM Shorts s " +
            "JOIN FETCH s.user " +
            "JOIN FETCH s.category " +
            "WHERE s.id = :id")
    Optional<Shorts> findWithDetailsById(@Param("id") Long id);

    /**
     * 상세 조회 - Keyword 포함
     */
    @Query("SELECT DISTINCT s FROM Shorts s " +
            "JOIN FETCH s.user " +
            "JOIN FETCH s.category " +
            "LEFT JOIN FETCH s.shortsKeywords sk " +
            "LEFT JOIN FETCH sk.keyword " +
            "WHERE s.id = :id")
    Optional<Shorts> findWithDetailsAndKeywordsById(@Param("id") Long id);

    /**
     * 목록 조회 - 기본 (페이징)
     */
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findAll(Pageable pageable);

    // ============================================
    // Status별 조회
    // ============================================

    /**
     * Status별 조회
     */
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByStatus(ShortsStatus status, Pageable pageable);

    /**
     * 랜덤 PUBLISHED 숏츠 조회
     */
    @Query(value = "SELECT s.* FROM shorts s " +
            "WHERE s.status = 'PUBLISHED' " +
            "ORDER BY RAND()",
            countQuery = "SELECT COUNT(*) FROM shorts WHERE status = 'PUBLISHED'",
            nativeQuery = true)
    Page<Shorts> findRandomPublishedShorts(Pageable pageable);

    // ============================================
    // 사용자별 조회
    // ============================================

    /**
     * 특정 사용자의 숏츠 목록 조회
     */
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByUserId(Long userId, Pageable pageable);

    // ============================================
    // 카테고리별 조회
    // ============================================

    /**
     * 카테고리별 숏츠 목록 조회
     */
    @EntityGraph(attributePaths = {"user", "category"})
    Page<ShortsResponse> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * 카테고리 + Status 조회
     */
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByCategoryIdAndStatus(Long categoryId, ShortsStatus status, Pageable pageable);


    // ============================================
    // 인기 숏츠 조회
    // ============================================

    /**
     * 인기 숏츠 조회 (likeCount 기준)
     * - 최근 N일 이내
     * - PUBLISHED 상태만
     */
    @Query("SELECT s FROM Shorts s " +
            "WHERE s.status = 'PUBLISHED' " +
            "AND s.createdAt >= :since " +
            "ORDER BY s.likeCount DESC, s.createdAt DESC")
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findPopularShorts(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 인기 숏츠 조회 (Like 테이블 JOIN 버전)
     * - likeCount 컬럼이 없을 때 사용
     */
    @Query("SELECT s FROM Shorts s " +
            "LEFT JOIN ShortsLike l ON l.shorts = s " +
            "WHERE s.status = 'PUBLISHED' " +
            "AND s.createdAt >= :since " +
            "GROUP BY s.id " +
            "ORDER BY COUNT(l.id) DESC, s.createdAt DESC")
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findPopularShortsByLikes(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 특정 ID를 제외한 숏츠 조회
     */
    List<Shorts> findByIdNot(Long shortsId);

    /**
     * 랜덤 조회 (전체) - 레거시
     */
    @Query(value = "SELECT s.* FROM shorts s ORDER BY RAND()",
            countQuery = "SELECT COUNT(*) FROM shorts",
            nativeQuery = true)
    Page<Shorts> findAllRandom(Pageable pageable);

    @Query(value = "SELECT * FROM shorts s " +
            "WHERE s.id != :shortsId " +
            "AND s.status = :status " +
            "ORDER BY RAND() " +
            "LIMIT 10", nativeQuery = true)
    List<Shorts> findRecommendationCandidates
            (@Param("shortsId") Long shortsId,
             @Param("status") String status);
}