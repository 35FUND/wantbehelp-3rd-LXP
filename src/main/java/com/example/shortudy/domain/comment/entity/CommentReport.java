package com.example.shortudy.domain.comment.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 댓글 신고 엔티티
 */
@Entity
@Getter
@Table(name = "comment_reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CommentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long commentId; // 신고 대상 댓글 ID

    @Column(nullable = false)
    private Long reporterId; // 신고자 ID

    @Column(nullable = false)
    private String reason; // 신고 사유

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status; // 신고 처리 상태

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public CommentReport(Long commentId, Long reporterId, String reason) {
        this.commentId = commentId;
        this.reporterId = reporterId;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    public void process() {
        this.status = ReportStatus.PROCESSED;
    }
}
