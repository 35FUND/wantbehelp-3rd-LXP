package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShortsRepositoryImpl implements ShortsRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    /**
     * [상세 조회 통합 쿼리 상세 분석]
     * 1. 목적: 클라이언트가 쇼츠 상세 페이지 진입 시 필요한 '본문+작성자+카테고리+통계' 정보를 한 번에 제공.
     * 2. 데이터 구조:
     *    - s: Shorts 메인 엔티티 정보
     *    - (Subquery 1): Comment 테이블을 shortsId로 스캔하여 count 집계 (댓글 총합)
     *    - (Subquery 2): ShortsLike 테이블에서 (shortsId + userId) 조합이 존재하는지 확인 (좋아요 여부)
     * 3. 성능 포인트: 
     *    - JOIN FETCH를 사용함으로써 s.getUser()나 s.getCategory() 호출 시 추가 SELECT 쿼리가 나가지 않음.
     *    - JPA의 엔티티 조회와 일반 SQL의 스칼라 조회를 혼합하여 배열(Object[])로 수신.
     */
    @Override
    public Optional<Object[]> findShortsWithCounts(Long id, Long userId) {
        String jpql = "SELECT s, " +
                "(SELECT count(cm) FROM Comment cm WHERE cm.shorts = s), " +
                "(SELECT count(l) > 0 FROM ShortsLike l WHERE l.shorts = s AND l.userId = :userId) " +
                "FROM Shorts s " +
                "JOIN FETCH s.user " +
                "JOIN FETCH s.category " +
                "WHERE s.id = :id";

        List<Object[]> results = em.createQuery(jpql, Object[].class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getResultList();

        return results.stream().findFirst();
    }

    /**
     * [전체 목록 조회 쿼리 상세 분석]
     * 1. 작동 방식: 
     *    - Content 조회 시에는 FETCH JOIN을 통해 N+1을 방어하고 서브쿼리로 통계 데이터를 병합.
     *    - Count 조회 시에는 조인 없이 조건절만 사용하여 집계 속도 극대화.
     * 2. 정렬 로직: 기본적으로 최신순(id DESC)으로 정렬하여 사용자에게 최신 컨텐츠 노출.
     */
    @Override
    public Page<Object[]> findShortsPageWithCounts(ShortsStatus status, Long userId, Pageable pageable) {
        String baseJpql = "FROM Shorts s JOIN FETCH s.user JOIN FETCH s.category WHERE s.status = :status";
        return fetchPage(baseJpql, userId, pageable, status, null);
    }

    /**
     * [카테고리별 필터링 조회 상세 분석]
     * - 클라이언트가 카테고리 탭을 선택했을 때 동작.
     * - 'Category.id = :categoryId' 조건을 통해 DB 인덱스를 타고 빠르게 필터링 수행.
     */
    @Override
    public Page<Object[]> findByCategoryWithCounts(Long categoryId, ShortsStatus status, Long userId, Pageable pageable) {
        String baseJpql = "FROM Shorts s JOIN FETCH s.user JOIN FETCH s.category WHERE s.category.id = :categoryId AND s.status = :status";
        return fetchPage(baseJpql, userId, pageable, status, categoryId);
    }

    /**
     * [인기 숏츠(Trending) 조회 상세 분석]
     * 1. 랭킹 로직: 
     *    - 'likeCount DESC': 좋아요를 많이 받은 순서대로 정렬.
     *    - 'id DESC': 좋아요 수가 같다면 최신 작성순으로 정렬.
     * 2. 기간 제한: 'createdAt >= :since' 조건을 통해 최근 트렌드(예: 30일 이내) 컨텐츠만 선별.
     */
    @Override
    public Page<Object[]> findPopularWithCounts(LocalDateTime since, Long userId, Pageable pageable) {
        String baseJpql = "FROM Shorts s JOIN FETCH s.user JOIN FETCH s.category WHERE s.status = 'PUBLISHED' AND s.createdAt >= :since";
        
        String selectJpql = "SELECT s, " +
                "(SELECT count(cm) FROM Comment cm WHERE cm.shorts = s), " +
                "(SELECT count(l) > 0 FROM ShortsLike l WHERE l.shorts = s AND l.userId = :userId) " +
                baseJpql + " ORDER BY s.likeCount DESC, s.id DESC";

        TypedQuery<Object[]> query = em.createQuery(selectJpql, Object[].class)
                .setParameter("userId", userId)
                .setParameter("since", since)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<Object[]> content = query.getResultList();
        
        // Count 쿼리 최적화: 불필요한 JOIN FETCH와 SELECT 항목을 제거하여 순수 개수만 집계
        String countJpql = "SELECT count(s) FROM Shorts s WHERE s.status = 'PUBLISHED' AND s.createdAt >= :since";
        Long total = em.createQuery(countJpql, Long.class)
                .setParameter("since", since)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * [마이페이지 조회 상세 분석]
     * 1. 소유권 확인: 's.user.id = :ownerId'를 통해 본인이 작성한 영상만 노출.
     * 2. 연관 정보: 본인 영상이라도 댓글수와 (타인의 좋아요와 구분되는) 본인의 인터랙션 여부를 확인하기 위해 집계 포함.
     */
    @Override
    public Page<Object[]> findMyShortsWithCounts(Long userId, Pageable pageable) {
        String baseJpql = "FROM Shorts s JOIN FETCH s.user JOIN FETCH s.category WHERE s.user.id = :ownerId";
        
        String selectJpql = "SELECT s, " +
                "(SELECT count(cm) FROM Comment cm WHERE cm.shorts = s), " +
                "(SELECT count(l) > 0 FROM ShortsLike l WHERE l.shorts = s AND l.userId = :requestUserId) " +
                baseJpql + " ORDER BY s.id DESC";

        TypedQuery<Object[]> query = em.createQuery(selectJpql, Object[].class)
                .setParameter("ownerId", userId)
                .setParameter("requestUserId", userId)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<Object[]> content = query.getResultList();

        String countJpql = "SELECT count(s) FROM Shorts s WHERE s.user.id = :ownerId";
        Long total = em.createQuery(countJpql, Long.class)
                .setParameter("ownerId", userId)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * [데이터 조회 공통 로직]
     * - 중복되는 JPQL 생성 및 페이징 파라미터 설정을 한 곳에서 관리.
     * - Content 쿼리와 Count 쿼리를 철저히 분리하여 페이징 엔진의 부하 감소.
     */
    private Page<Object[]> fetchPage(String baseJpql, Long userId, Pageable pageable, ShortsStatus status, Long categoryId) {
        String selectJpql = "SELECT s, " +
                "(SELECT count(cm) FROM Comment cm WHERE cm.shorts = s), " +
                "(SELECT count(l) > 0 FROM ShortsLike l WHERE l.shorts = s AND l.userId = :userId) " +
                baseJpql + " ORDER BY s.id DESC";

        TypedQuery<Object[]> query = em.createQuery(selectJpql, Object[].class)
                .setParameter("userId", userId)
                .setParameter("status", status);
        
        if (categoryId != null) query.setParameter("categoryId", categoryId);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Object[]> content = query.getResultList();

        // [최적화] 집계 쿼리에서는 FETCH JOIN이 성능 저하를 유발하므로 순수 엔티티 카운트 쿼리만 수행
        String countJpql = "SELECT count(s) FROM Shorts s WHERE " + (categoryId != null ? "s.category.id = :categoryId AND " : "") + "s.status = :status";
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class)
                .setParameter("status", status);
        
        if (categoryId != null) countQuery.setParameter("categoryId", categoryId);
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
