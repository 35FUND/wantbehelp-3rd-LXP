package com.example.shortudy.domain.like.entity;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.global.util.AssertUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(
        name = "shorts_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "shorts_id", "deleted_at"})
)
@SQLDelete(sql = "UPDATE shorts_like SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ShortsLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorts_id", nullable = false)
    private Shorts shorts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected ShortsLike() {}

    private ShortsLike(User user, Shorts shorts) {
        this.user = user;
        this.shorts = shorts;
    }

    /**
     * 취소 된 좋아요를 다시 누를떄 Sort delete 된 부분을 재활용
     * 계속 좋아요를 새로 만들게 되면, 무의미한 데이터가 쌓일 것이라 되살리는 로직을 추가
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * Soft Delete 상태인지 확인
     * @return soft delete 여부(true / false)
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 좋아요 생성
     * @param user 좋아요 누른 유저 정보
     * @param shorts 좋아요 누른 대상 숏츠
     * @return 좋아요 엔티티 객체
     */
    public static ShortsLike of(User user, Shorts shorts) {
        AssertUtil.notNull(user, "유저 정보는 필수입니다");
        AssertUtil.notNull(shorts, "숏츠 정보는 필수입니다");

        return new ShortsLike(user, shorts);
    }
}
