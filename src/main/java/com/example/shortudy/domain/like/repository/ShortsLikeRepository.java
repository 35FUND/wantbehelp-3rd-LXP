package com.example.shortudy.domain.like.repository;

import com.example.shortudy.domain.like.entity.ShortsLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortsLikeRepository extends JpaRepository<ShortsLike, Long> {

    /**
     * 사용자 ID, 숏츠 ID 기반 삭제(Soft Delete) 되지 않은 좋아요 찾기
     * @param userId 사용자 ID
     * @param shortsId 숏츠 ID
     * @return 삭제 처리 된 좋아요 엔티티 객체(Optional)
     */
    @Query(value = "SELECT * FROM shorts_like " +
            "WHERE user_id = :userId " +
            "AND shorts_id = :shortsId",
            nativeQuery = true)
    Optional<ShortsLike> findWithDeleted(Long userId, Long shortsId);

    /**
     * 좋아요에 관련된 모든 정보 받아오기(shorts, user, category) - 최신순
     * @param userId 사용자 ID
     * @return 좋아요와 관련된 최신순 정보 리스트
     */
    @Query("SELECT sl FROM ShortsLike sl " +
            "JOIN FETCH sl.shorts s " +
            "JOIN FETCH s.user u " +
            "JOIN FETCH s.category c " +
            "WHERE sl.user.id = :userId " +
            "ORDER BY sl.createdAt DESC, sl.id DESC")
    Page<ShortsLike> findAllByUserIdWithDetailsLatest(@Param("userId") Long userId, Pageable pageable);

    /**
     * 좋아요에 관련된 모든 정보 받아오기(shorts, user, category) - 인기순(like count)
     * @param userId 사용자 ID
     * @return 좋아요와 관련된 인기순 정보 리스트
     */
    @Query("SELECT sl FROM ShortsLike sl " +
            "JOIN FETCH sl.shorts s " +
            "JOIN FETCH s.user u " +
            "JOIN FETCH s.category c " +
            "WHERE sl.user.id = :userId " +
            "ORDER BY s.likeCount DESC, sl.createdAt DESC")
    Page<ShortsLike> findAllByUserIdWithDetailsPopular(@Param("userId") Long userId, Pageable pageable);

    Optional<ShortsLike> findByUserIdAndShortsId(Long userId, Long shortsId);

    // 내 좋아요 목록 조회 (Batch)
    @Query("select sl from ShortsLike sl where sl.user.id = :userId and sl.shorts.id in :shortsIds")
    List<ShortsLike> findByUserIdAndShortsIdIn(@Param("userId") Long userId, @Param("shortsIds") List<Long> shortsIds);

    boolean existsByUserIdAndShortsId(Long userId, Long shortsId);

    public interface ShortsLikeCountProjection {
        Long getShortsId();

        long getCnt();
    }

    // 좋아요 개수 조회
    @Query("""
              select sl.shorts.id as shortsId, count(sl.id) as cnt
              from ShortsLike sl
              where sl.shorts.id in :shortsIds
              group by sl.shorts.id
            """)
    List<ShortsLikeCountProjection> countLikesByShortsIds(@Param("shortsIds") List<Long> shortsIds);

    // 숏츠 삭제 시 좋아요 전부 삭제
    @Modifying(clearAutomatically = true) // 변경 감지(영속성 컨텍스트 1차 캐싱) 초기화
    void deleteByShortsId(Long shortsId);
}


