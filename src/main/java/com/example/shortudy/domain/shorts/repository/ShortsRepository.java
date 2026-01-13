package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.shorts.entity.Shorts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortsRepository extends JpaRepository<Shorts, Long> {

    /**
     * 상세 조회 - 연관 엔티티 모두 fetch join
     * N+1 문제 방지
     */
    // TODO - Tag -> Keyword 변경에 따른 수정 필요 (태그 연관관계가 복구되면 fetch join 확장)
    @Query("SELECT s FROM Shorts s " +
           "JOIN FETCH s.user " +
           "JOIN FETCH s.category " +
           "WHERE s.id = :id")
    Optional<Shorts> findWithDetailsById(@Param("id") Long id);

    /**
     * 목록 조회 - 기본 정보만 (페이징)
     * taggings는 별도 쿼리로 조회 (batch size 설정 권장)
     */
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findAll(Pageable pageable);

    /**
     * 특정 사용자의 숏폼 목록 조회
     */
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByUserId(Long userId, Pageable pageable);

    /**
     * 랜덤 정렬 목록 조회 (숏폼 피드용)
     */
    @Query(value = "SELECT s.* FROM shorts_form s ORDER BY RAND()",
           countQuery = "SELECT COUNT(*) FROM shorts_form",
           nativeQuery = true)
    Page<Shorts> findAllRandom(Pageable pageable);

    List<Shorts> findByIdNot(Long shortsIdLong);
}
