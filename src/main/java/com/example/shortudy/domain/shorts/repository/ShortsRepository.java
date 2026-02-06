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

    @Modifying
    @Query("UPDATE Shorts s SET s.viewCount = s.viewCount + :count WHERE s.id = :id")
    void updateViewCount(@Param("id") Long id, @Param("count") Long count);

    @Query("SELECT new com.example.shortudy.domain.shorts.dto.ShortsResponse(" +
            "s.id, s.title, s.description, s.videoUrl, s.thumbnailUrl, s.durationSec, s.status, " +
            "u.id, COALESCE(u.nickname, '알 수 없음'), u.profileUrl, " +
            "c.id, c.name, " +
            "null, " + 
            "s.viewCount, s.likeCount, " +
            "(SELECT count(cm) FROM Comment cm WHERE cm.shorts = s), " +
            "s.createdAt, s.updatedAt, " +
            "(SELECT count(l) > 0 FROM ShortsLike l WHERE l.shorts = s AND l.user.id = :userId)) " +
            "FROM Shorts s " +
            "LEFT JOIN s.user u " +
            "JOIN s.category c " +
            "WHERE s.id = :id")
    Optional<ShortsResponse> findResponseById(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT new com.example.shortudy.domain.shorts.dto.ShortsResponse(" +
            "s.id, s.title, s.description, s.videoUrl, s.thumbnailUrl, s.durationSec, s.status, " +
            "u.id, COALESCE(u.nickname, '알 수 없음'), u.profileUrl, " +
            "c.id, c.name, " +
            "null, " + 
            "s.viewCount, s.likeCount, " +
            "(SELECT count(cm) FROM Comment cm WHERE cm.shorts = s), " +
            "s.createdAt, s.updatedAt, " +
            "(SELECT count(l) > 0 FROM ShortsLike l WHERE l.shorts = s AND l.user.id = :userId)) " +
            "FROM Shorts s " +
            "LEFT JOIN s.user u " +
            "JOIN s.category c " +
            "WHERE s.status = :status")
    Page<ShortsResponse> findResponsesByStatus(@Param("status") ShortsStatus status, @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.example.shortudy.domain.shorts.dto.ShortsResponse(" +
            "s.id, s.title, s.description, s.videoUrl, s.thumbnailUrl, s.durationSec, s.status, " +
            "u.id, COALESCE(u.nickname, '알 수 없음'), u.profileUrl, " +
            "c.id, c.name, " +
            "null, " + 
            "s.viewCount, s.likeCount, " +
            "(SELECT count(cm) FROM Comment cm WHERE cm.shorts = s), " +
            "s.createdAt, s.updatedAt, " +
            "(SELECT count(l) > 0 FROM ShortsLike l WHERE l.shorts = s AND l.user.id = :userId)) " +
            "FROM Shorts s " +
            "LEFT JOIN s.user u " +
            "JOIN s.category c " +
            "WHERE s.category.id = :categoryId AND s.status = :status")
    Page<ShortsResponse> findResponsesByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") ShortsStatus status, @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.example.shortudy.domain.shorts.dto.ShortsResponse(" +
            "s.id, s.title, s.description, s.videoUrl, s.thumbnailUrl, s.durationSec, s.status, " +
            "u.id, COALESCE(u.nickname, '알 수 없음'), u.profileUrl, " +
            "c.id, c.name, " +
            "null, " + 
            "s.viewCount, s.likeCount, " +
            "(SELECT count(cm) FROM Comment cm WHERE cm.shorts = s), " +
            "s.createdAt, s.updatedAt, " +
            "(SELECT count(l) > 0 FROM ShortsLike l WHERE l.shorts = s AND l.user.id = :userId)) " +
            "FROM Shorts s " +
            "LEFT JOIN s.user u " +
            "JOIN s.category c " +
            "WHERE s.status = 'PUBLISHED' AND s.createdAt >= :since")
    Page<ShortsResponse> findPopularResponses(@Param("since") LocalDateTime since, @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.example.shortudy.domain.shorts.dto.ShortsResponse(" +
            "s.id, s.title, s.description, s.videoUrl, s.thumbnailUrl, s.durationSec, s.status, " +
            "u.id, u.nickname, u.profileUrl, " +
            "c.id, c.name, " +
            "null, " + 
            "s.viewCount, s.likeCount, " +
            "(SELECT count(cm) FROM Comment cm WHERE cm.shorts = s), " +
            "s.createdAt, s.updatedAt, " +
            "(SELECT count(l) > 0 FROM ShortsLike l WHERE l.shorts = s AND l.user.id = :userId)) " +
            "FROM Shorts s " +
            "JOIN s.user u " +
            "JOIN s.category c " +
            "WHERE s.user.id = :userId")
    Page<ShortsResponse> findMyResponses(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT s FROM Shorts s " +
            "JOIN FETCH s.user " +
            "JOIN FETCH s.category " +
            "LEFT JOIN FETCH s.shortsKeywords sk " +
            "LEFT JOIN FETCH sk.keyword " +
            "WHERE s.id = :id")
    Optional<Shorts> findWithDetailsAndKeywordsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByStatus(ShortsStatus status, Pageable pageable);

    /**
     * [랜덤 쇼츠 조회]
     */
    @Query("SELECT s FROM Shorts s WHERE s.status = 'PUBLISHED' ORDER BY rand()")
    Page<Shorts> findRandomPublishedShorts(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByCategoryIdAndStatus(Long categoryId, ShortsStatus status, Pageable pageable);

    @Query("SELECT s FROM Shorts s " +
            "WHERE s.status = 'PUBLISHED' " +
            "AND s.createdAt >= :since " +
            "ORDER BY s.likeCount DESC, s.createdAt DESC")
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findPopularShorts(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT s FROM Shorts s " +
            "LEFT JOIN ShortsLike l ON l.shorts = s " +
            "WHERE s.status = 'PUBLISHED' " +
            "AND s.createdAt >= :since " +
            "GROUP BY s.id " +
            "ORDER BY COUNT(l.id) DESC, s.createdAt DESC")
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findPopularShortsByLikes(@Param("since") LocalDateTime since, Pageable pageable);

    List<Shorts> findByIdNot(Long shortsId);

    /**
     * [전체 랜덤 조회]
     */
    @Query("SELECT s FROM Shorts s ORDER BY rand()")
    Page<Shorts> findAllRandom(Pageable pageable);

    /**
     * [추천 후보 숏츠 조회]
     * 1. 목적: 추천 알고리즘을 수행할 대상 후보군을 추출합니다.
     * 2. 로직: 현재 보고 있는 영상을 제외하고 발행된 영상들을 무작위로 가져옵니다.
     * 3. 제한: 대량 조회를 방지하기 위해 Pageable을 통해 후보군 크기를 제한합니다.
     */
    @Query("SELECT s FROM Shorts s " +
            "WHERE s.id != :shortsId " +
            "AND s.status = :status " +
            "ORDER BY rand()")
    List<Shorts> findRecommendationCandidates(
            @Param("shortsId") Long shortsId,
            @Param("status") ShortsStatus status,
            Pageable pageable);
}

