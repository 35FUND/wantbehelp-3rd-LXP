package com.example.shortudy.domain.playlist.playlist.entity;

import com.example.shortudy.domain.shorts.entity.Shorts;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 플레이리스트-숏츠 연결 엔티티 (중간 테이블)
 *   Playlist (1) --- (*) PlaylistShorts (*) --- (1) Shorts
 * [테이블 구조]
 * playlist_shorts 테이블:
 * | id | playlist_id | shorts_id | position | added_at |
 * |----|-------------|-----------|----------|----------|
 * | 1  | 1           | 10        | 0        | 2024-... |
 * | 2  | 1           | 20        | 1        | 2024-... |
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "playlist_shorts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"playlist_id", "shorts_id"}),
        indexes = {
                @Index(name = "idx_playlist_shorts_position", columnList = "playlist_id, position")
        }
)
public class PlaylistShorts {

    /**
     * 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이 항목이 속한 플레이리스트
     * [양방향 관계에서의 역할]
     * - Playlist 엔티티의 playlistShorts 리스트와 연결됨
     * - 여기서 playlist 필드가 "연관관계의 주인"
     * */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    /**
     * 플레이리스트에 담긴 숏츠
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorts_id", nullable = false)
    private Shorts shorts;

    /**
     * 플레이리스트 내에서의 순서 (0부터 시작)
     * [왜 순서를 별도로 저장?]
     * - 사용자가 원하는 순서대로 숏츠를 재생하기 위함
     * - 순서 변경 시 이 값만 업데이트하면 됨
     */
    @Column(name = "position", nullable = false)
    private Integer position;

    /**
     * 플레이리스트에 추가된 시간
     * - @CreatedDate: 이 엔티티가 처음 저장될 때 자동으로 현재 시간 기록
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    /**
     * JPA용 기본 생성자
     */
    protected PlaylistShorts() {
    }

    /**
     * private 생성자 - create() 메서드를 통해서만 객체 생성
     */
    private PlaylistShorts(Playlist playlist, Shorts shorts, int position) {
        this.playlist = playlist;
        this.shorts = shorts;
        this.position = position;
    }

    /**
     * PlaylistShorts 객체 생성 (정적 팩토리 메서드)
     *
     * @param playlist 숏츠를 담을 플레이리스트
     * @param shorts   추가할 숏츠
     * @param position 순서 번호
     * @return 생성된 PlaylistShorts 객체
     */
    public static PlaylistShorts create(Playlist playlist, Shorts shorts, int position) {
        return new PlaylistShorts(playlist, shorts, position);
    }

    /**
     * 순서 번호 변경
     * - 플레이리스트 내 숏츠 순서 재배치 시 호출됨
     *
     * @param newPosition 새로운 순서 번호
     */
    public void updatePosition(int newPosition) {
        this.position = newPosition;
    }
}
