package com.example.shortudy.domain.playlist.entity;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * [이 클래스의 역할]
 * - 사용자가 만든 "플레이리스트" 정보를 저장합니다.
 * - 플레이리스트 안에 담긴 숏츠 목록도 함께 관리합니다.
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "playlists")
public class Playlist {
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_THUMBNAIL_URL_LENGTH = 500; // @Column length 참조용


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)  // DB의 외래키 컬럼명 지정
    private User user;


    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;


    @Column(length = MAX_DESCRIPTION_LENGTH)
    private String description;

    /**
     * 공개 여부 (PUBLIC: 전체공개, PRIVATE: 비공개)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaylistVisibility visibility;

    /**
     * 플레이리스트 썸네일 이미지 URL
     */
    @Column(name = "thumbnail_url", length = MAX_THUMBNAIL_URL_LENGTH)
    private String thumbnailUrl;

    /**
     * 썸네일이 사용자 지정인지 여부 (true면 자동 변경 금지)
     */
    @ColumnDefault("false")
    @Column(name = "thumbnail_custom", nullable = false, columnDefinition = "boolean default false")
    private boolean thumbnailCustom;

    /**
     * 이 플레이리스트에 담긴 숏츠 목록
     * [cascade = CascadeType.ALL]
     * - 플레이리스트를 저장/삭제하면 안에 담긴 숏츠 목록도 함께 저장/삭제됨
     * - 부모(Playlist)의 변경이 자식(PlaylistShorts)에게 전파됨
     * [orphanRemoval = true]
     * - 목록에서 제거된 항목은 DB에서도 삭제됨
     * - "고아 객체 제거" = 부모와 연결이 끊어진 자식을 자동 삭제
     * [@OrderBy("position ASC")]
     * - 조회할 때 position 기준 오름차순 정렬
     */
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PlaylistShorts> playlistShorts = new ArrayList<>();

    /**
     * 생성 일시
     * - @CreatedDate: JPA가 엔티티 생성 시 자동으로 현재 시간 기록
     * - updatable = false: 한번 저장되면 수정 불가
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     * - @LastModifiedDate: 엔티티가 수정될 때마다 자동으로 현재 시간으로 갱신
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 기본 생성자 (JPA 필수)
     * [protected로 선언한 이유]
     * - 외부에서 new Playlist()로 직접 만드는 것은 막기 위해서
     */
    protected Playlist() {
    }

    private Playlist(User user, String title, String description, PlaylistVisibility visibility) {
        validateTitle(title);
        validateDescription(description);

        this.user = user;
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.playlistShorts = new ArrayList<>();
        this.thumbnailCustom = false;
    }

    /**
     * 플레이리스트 생성 (정적 팩토리 메서드) - 기본 비공개
     */
    public static Playlist create(User user, String title, String description) {
        return new Playlist(user, title, description, PlaylistVisibility.PRIVATE);
    }

    /**
     * 플레이리스트 생성 (정적 팩토리 메서드) - 공개 여부 지정
     */
    public static Playlist create(User user, String title, String description, PlaylistVisibility visibility) {
        return new Playlist(user, title, description, visibility);
    }

    /**
     * 플레이리스트 정보 수정
     * [null 체크하는 이유]
     * - PATCH 요청에서는 변경하고 싶은 필드만 보내고, 나머지는 null로 올 수 있음
     * - null인 필드는 기존 값 유지, 값이 있는 필드만 업데이트
     */
    public void update(String title, String description, PlaylistVisibility visibility) {
        if (title != null && !title.isBlank()) {
            validateTitle(title);
            this.title = title;
        }
        if (description != null) {
            validateDescription(description);
            this.description = description;
        }
        if (visibility != null) {
            this.visibility = visibility;
        }
    }

    /**
     * 플레이리스트 내 특정 숏츠의 썸네일을 플레이리스트 썸네일로 지정
     * - 해당 숏츠가 플레이리스트에 포함되어 있어야 함
     *
     * @param shortsId 썸네일로 사용할 숏츠 ID
     */
    public void selectThumbnailFromShorts(Long shortsId) {
        String selectedUrl = playlistShorts.stream()
                .filter(ps -> ps.getShorts().getId().equals(shortsId))
                .map(ps -> ps.getShorts().getThumbnailUrl())
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.PLAYLIST_ITEM_NOT_FOUND,
                        "플레이리스트에 포함된 숏츠만 썸네일로 지정할 수 있습니다."));

        this.thumbnailUrl = selectedUrl;
        this.thumbnailCustom = true;
    }

    /**
     * 사용자 지정 썸네일 해제 (자동 썸네일 사용 - 첫 번째 숏츠 썸네일)
     */
    public void clearCustomThumbnail() {
        this.thumbnailCustom = false;
        this.thumbnailUrl = getFirstShortsThumbnail();
    }

    /**
     * 플레이리스트에 숏츠 추가
     * [로직 설명]
     * 1. 이미 추가된 숏츠인지 확인 (중복 방지)
     * 2. 새 숏츠의 순서 번호 = 현재 목록 크기 (0부터 시작하므로)
     * 3. PlaylistShorts 객체 생성하여 목록에 추가
     * 4. 첫 번째 숏츠라면 그 썸네일을 플레이리스트 썸네일로 설정
     *
     * @param shorts 추가할 숏츠
     * @return 생성된 PlaylistShorts 객체
     */
    public PlaylistShorts addShorts(Shorts shorts) {
        // stream(): 리스트를 하나씩 순회하면서 처리하는 방법
        // anyMatch(): 조건에 맞는 항목이 하나라도 있으면 true
        boolean exists = playlistShorts.stream()
                .anyMatch(ps -> ps.getShorts().getId().equals(shorts.getId()));
        if (exists) {
            throw new BaseException(ErrorCode.ALREADY_ADDED_SHORTS);
        }

        int nextOrder = playlistShorts.size();  // 예: 3개 있으면 다음은 index 3
        PlaylistShorts newItem = PlaylistShorts.create(this, shorts, nextOrder);
        this.playlistShorts.add(newItem);

        refreshThumbnailIfAuto();

        return newItem;
    }

    /**
     * 플레이리스트에서 숏츠 제거
     * [로직 설명]
     * 1. 제거할 숏츠 찾기
     * 2. 목록에서 제거
     * 3. 제거된 위치보다 뒤에 있던 항목들의 순서를 1씩 당김
     *    예: [A:0, B:1, C:2, D:3]에서 B 제거 → [A:0, C:1, D:2]
     *
     * @param shortsId 제거할 숏츠 ID
     */
    public void removeShorts(Long shortsId) {
        // findFirst(): 조건에 맞는 첫 번째 항목을 Optional로 반환
        Optional<PlaylistShorts> target = playlistShorts.stream()
                .filter(ps -> ps.getShorts().getId().equals(shortsId))
                .findFirst();

        if (target.isEmpty()) {
            throw new BaseException(ErrorCode.PLAYLIST_ITEM_NOT_FOUND);
        }

        int removedPosition = target.get().getPosition();
        playlistShorts.remove(target.get());

        // 삭제된 위치 뒤의 항목들 순서 재조정 (-1씩)
        playlistShorts.stream()
                .filter(ps -> ps.getPosition() > removedPosition)
                .forEach(ps -> ps.updatePosition(ps.getPosition() - 1));

        refreshThumbnailIfAuto();
    }

    /**
     * 플레이리스트 내 숏츠 순서 변경
     * [순서 변경 알고리즘]
     * 예시: [A:0, B:1, C:2, D:3, E:4]에서 B(index 1)를 index 3으로 이동
     * 1. 아래로 이동하는 경우 (currentIndex < newIndex):
     *    - B와 새 위치 사이의 항목들(C, D)을 위로 당김 (-1)
     *    - [A:0, B:1, C:1, D:2, E:4] → [A:0, C:1, D:2, B:3, E:4]
     * 2. 위로 이동하는 경우 (currentIndex > newIndex):
     *    - 새 위치와 D 사이의 항목들을 아래로 밀어냄 (+1)
     *
     * @param shortsId 이동할 숏츠 ID
     * @param newIndex 새로운 위치 (0부터 시작)
     */
    public void reorderShorts(Long shortsId, int newIndex) {
        // 유효한 인덱스 범위인지 확인
        if (newIndex < 0 || newIndex >= playlistShorts.size()) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }

        Optional<PlaylistShorts> target = playlistShorts.stream()
                .filter(ps -> ps.getShorts().getId().equals(shortsId))
                .findFirst();

        if (target.isEmpty()) {
            throw new BaseException(ErrorCode.PLAYLIST_ITEM_NOT_FOUND);
        }

        int currentPosition = target.get().getPosition();
        if (currentPosition == newIndex) {
            return;  // 같은 위치면 아무것도 안 함
        }

        if (currentPosition < newIndex) {
            // 아래로 이동: 사이 항목들을 위로 당김
            playlistShorts.stream()
                    .filter(ps -> ps.getPosition() > currentPosition && ps.getPosition() <= newIndex)
                    .forEach(ps -> ps.updatePosition(ps.getPosition() - 1));
        } else {
            // 위로 이동: 사이 항목들을 아래로 밀어냄
            playlistShorts.stream()
                    .filter(ps -> ps.getPosition() >= newIndex && ps.getPosition() < currentPosition)
                    .forEach(ps -> ps.updatePosition(ps.getPosition() + 1));
        }

        target.get().updatePosition(newIndex);

        refreshThumbnailIfAuto();
    }

    /**
     * 공개 플레이리스트인지 확인
     */
    public boolean isPublic() {
        return this.visibility == PlaylistVisibility.PUBLIC;
    }

    /**
     * 특정 사용자가 이 플레이리스트의 소유자인지 확인
     * [Null Safety]
     * - userId가 null이면 절대 소유자가 아님 (false 반환)
     * - NPE 방지
     */
    public boolean isOwner(Long userId) {
        if (userId == null) {
            return false;
        }
        return this.user.getId().equals(userId);
    }

    /**
     * 플레이리스트에 담긴 숏츠 개수 반환
     */
    public int getShortsCount() {
        return playlistShorts.size();
    }

    private void refreshThumbnailIfAuto() {
        if (thumbnailCustom) {
            return;
        }
        this.thumbnailUrl = getFirstShortsThumbnail();
    }

    private String getFirstShortsThumbnail() {
        return playlistShorts.stream()
                .min((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(ps -> ps.getShorts().getThumbnailUrl())
                .orElse(null);
    }

    /**
     * 제목 유효성 검사
     * - 필수 입력
     * - 최대 길이 제한
     */
    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_INPUT, "플레이리스트 제목은 필수입니다.");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new BaseException(ErrorCode.INVALID_INPUT,
                    "플레이리스트 제목은 " + MAX_TITLE_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    /**
     * 설명 유효성 검사
     * - 선택 입력이지만, 입력 시 최대 길이 제한
     */
    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BaseException(ErrorCode.INVALID_INPUT,
                    "플레이리스트 설명은 " + MAX_DESCRIPTION_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

}
