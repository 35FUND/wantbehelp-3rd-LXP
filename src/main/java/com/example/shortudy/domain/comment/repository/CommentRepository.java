package com.example.shortudy.domain.comment.repository;

import com.example.shortudy.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 단일 숏츠의 댓글 개수 조회
    @Query("select count(c) from Comment c where c.shorts.id = :shortsId and c.status = 'ACTIVE'")
    long countByShortsId(@Param("shortsId") Long shortsId);

    // 전체 숏츠의 댓글 개수 조회
    @Query("""
              select c.shorts.id as shortsId, count(c.id) as cnt
              from Comment c
              where c.shorts.id in :shortsIds and c.status = 'ACTIVE'
              group by c.shorts.id
            """)
    List<ShortsCommentCountProjection> countAllCommentsByShortsIds(@Param("shortsIds") List<Long> shortsIds);

    // 댓글 조회
    @Query("""
                    SELECT c
                    FROM Comment c
                    JOIN FETCH c.user u
                    WHERE c.shorts.id = :shortsId
                    AND c.parent is null
                    ORDER BY c.createdAt desc
            """)
    List<Comment> findCommentsWithUser(@Param("shortsId") Long shortsId);

    // 대댓글 개수 조회
    @Query("""
                SELECT r.parent.id AS parentId, COUNT(r.id) AS cnt
                FROM Comment r
                WHERE r.parent.id IN :parentIds AND r.status = 'ACTIVE'
                GROUP BY r.parent.id
            """)
    List<ReplyCountProjection> countRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

    // 대댓글 조회
    @Query("""
                SELECT c
                FROM Comment c
                JOIN FETCH c.user u
                WHERE c.parent.id = :parentId
                ORDER BY c.createdAt ASC
            """)
    List<Comment> findRepliesWithUser(@Param("parentId") Long parentId);

    // 숏츠 삭제 시 댓글 전부 삭제
    @Modifying(clearAutomatically = true) // 변경 감지(영속성 컨텍스트 1차 캐싱) 초기화
    void deleteByShortsId(Long shortsId);

    // 부모 댓글 ID로 자식 댓글 조회 (삭제용)
    List<Comment> findAllByParentId(Long parentId);

    public interface ReplyCountProjection {

        Long getParentId();

        long getCnt();
    }

    public interface ShortsCommentCountProjection {
        Long getShortsId();

        long getCnt();
    }
}
