package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.shorts.entity.Shorts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortsRepository extends JpaRepository<Shorts, Long> {

    /**
     * 상세 조회 - 연관 엔티티 모두 fetch join
     * N+1 문제 방지
     */
    @Query("SELECT DISTINCT s FROM Shorts s " +
           "LEFT JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.category " +
           "LEFT JOIN FETCH s.taggings t " +
           "LEFT JOIN FETCH t.tag " +
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
}

