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

    @Query(value = "SELECT s.* FROM shorts s " +
            "WHERE s.status = 'PUBLISHED' " +
            "ORDER BY RAND()",
            countQuery = "SELECT COUNT(*) FROM shorts WHERE status = 'PUBLISHED'",
            nativeQuery = true)
    Page<Shorts> findRandomPublishedShorts(Pageable pageable);

    // nativeQuery = true로 변경하여 SQL 문법 사용
    @Query(value = "SELECT s.* FROM shorts s " +
            "WHERE s.status = 'PUBLISHED' " +
            "AND s.created_at >= :since " +
            "ORDER BY s.like_count DESC, s.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM shorts WHERE status = 'PUBLISHED' AND created_at >= :since",
            nativeQuery = true)
    Page<Shorts> findPopularShorts(@Param("since") LocalDateTime since, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Shorts s SET s.viewCount = s.viewCount + :count WHERE s.id = :shortsId")
    void incrementViewCount(@Param("shortsId") Long shortsId, @Param("count") Long count);

    @Query("SELECT s FROM Shorts s WHERE s.id != :excludeId AND s.status = :status")
    List<Shorts> findRecommendationCandidates(@Param("excludeId") Long excludeId, @Param("status") ShortsStatus status);
}
