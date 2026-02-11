package com.example.shortudy.domain.comment.repository;


import com.example.shortudy.domain.comment.entity.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 댓글 신고 저장소 인터페이스
 */
public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

    // 특정 댓글에 대해 특정 사용자가 이미 신고했는지 확인하는 메서드
    // 예) A 사용자가 B 댓글을 신고했는지 확인
    boolean existsByCommentIdAndReporterId(Long commentId, Long reporterId);
}
