package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortsRepository extends JpaRepository<Shorts, Long> {

    @Query("SELECT s FROM Shorts s " +
            "JOIN FETCH s.user " +
            "JOIN FETCH s.category " +
            "WHERE s.id = :id")
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "100"))
    Optional<Shorts> findWithDetailsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByStatus(ShortsStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByCategoryIdAndStatus(Long categoryId, ShortsStatus status, Pageable pageable);

    @Query(value = "SELECT s.* FROM shorts_form s " +
            "WHERE s.status = 'PUBLISHED' " +
            "ORDER BY RAND()",
            countQuery = "SELECT COUNT(*) FROM shorts_form WHERE status = 'PUBLISHED'",
            nativeQuery = true)
    Page<Shorts> findRandomPublishedShorts(Pageable pageable);

    @Query(value = "SELECT s.* FROM shorts_form s " +
            "WHERE s.status = 'PUBLISHED' " +
            "ORDER BY MD5(CONCAT(s.id, :seed))",
            countQuery = "SELECT COUNT(*) FROM shorts_form WHERE status = 'PUBLISHED'",
            nativeQuery = true)
    Page<Shorts> findPublishedShortsWithSeedOrder(@Param("seed") String seed, Pageable pageable);

    @Query("SELECT s FROM Shorts s WHERE s.id != :excludeId AND s.status = :status")
    List<Shorts> findRecommendationCandidates(@Param("excludeId") Long excludeId, @Param("status") ShortsStatus status);

    @Query("SELECT s FROM Shorts s " +
            "WHERE s.category.id = :categoryId " +
            "AND s.id != :excludeId " +
            "AND s.status = :status")
    List<Shorts> findCategoryRecommendationCandidates(@Param("categoryId") Long categoryId,
                                                      @Param("excludeId") Long excludeId,
                                                      @Param("status") ShortsStatus status);

    long countByUserIdAndStatus(Long userId, ShortsStatus status);

    long countByCategoryIdAndStatus(Long categoryId, ShortsStatus status);

    @Query("SELECT s FROM Shorts s WHERE s.id IN :ids")
    @EntityGraph(attributePaths = {"user", "category"})
    List<Shorts> findByIdsWithBasicRelations(@Param("ids") List<Long> ids);

    boolean existsByStatus(ShortsStatus status);

    @Deprecated
    List<Shorts> findByIdNot(Long shortsIdLong);

    @Deprecated
    @Query(value = "SELECT s.* FROM shorts_form s ORDER BY RAND()",
            countQuery = "SELECT COUNT(*) FROM shorts_form",
            nativeQuery = true)
    Page<Shorts> findAllRandom(Pageable pageable);

    /**
     * 카테고리별 숏츠 목록 조회
     * N+1 방지를 위해 EntityGraph 사용
     * (위쪽 중복 제거하고 이것만 살림)
     */
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * 인기 숏츠 조회
     * - 최근 N일 이내 업로드된 숏츠 중
     * - PUBLISHED 상태만
     * - 좋아요 수 내림차순, 생성일 내림차순 정렬
     */
    @Query("SELECT s FROM Shorts s " +
            "WHERE s.status = 'PUBLISHED' " +
            "AND s.createdAt >= :since " +
            "ORDER BY s.likeCount DESC, s.createdAt DESC")
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findPopularShorts(@Param("since") LocalDateTime since, Pageable pageable);
}