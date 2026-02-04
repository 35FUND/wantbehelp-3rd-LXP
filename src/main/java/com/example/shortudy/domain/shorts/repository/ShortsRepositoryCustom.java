package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 복잡한 집계 조회를 담당하는 사용자 정의 레포지토리 인터페이스
 */
public interface ShortsRepositoryCustom {

    /**
     * 단건 조회: 숏츠 엔티티 + 댓글수 + 좋아요 여부
     */
    Optional<Object[]> findShortsWithCounts(Long id, Long userId);

    /**
     * 목록 조회: 상태별 숏츠 + 댓글수 + 좋아요 여부
     */
    Page<Object[]> findShortsPageWithCounts(ShortsStatus status, Long userId, Pageable pageable);

    /**
     * 카테고리별 조회: 특정 카테고리 + 집계 데이터
     */
    Page<Object[]> findByCategoryWithCounts(Long categoryId, ShortsStatus status, Long userId, Pageable pageable);

    /**
     * 인기 조회: 최근 N일 + 집계 데이터
     */
    Page<Object[]> findPopularWithCounts(LocalDateTime since, Long userId, Pageable pageable);

    /**
     * 내 숏츠 조회: 사용자별 + 집계 데이터
     */
    Page<Object[]> findMyShortsWithCounts(Long userId, Pageable pageable);
}
