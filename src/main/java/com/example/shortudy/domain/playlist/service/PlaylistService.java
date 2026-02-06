package com.example.shortudy.domain.playlist.service;

import com.example.shortudy.domain.playlist.dto.request.PlaylistCreateRequest;
import com.example.shortudy.domain.playlist.dto.request.PlaylistShortsAddRequest;
import com.example.shortudy.domain.playlist.dto.request.PlaylistShortsReorderRequest;
import com.example.shortudy.domain.playlist.dto.request.PlaylistUpdateRequest;
import com.example.shortudy.domain.playlist.dto.response.PlaylistDetailResponse;
import com.example.shortudy.domain.playlist.dto.response.PlaylistResponse;
import com.example.shortudy.domain.playlist.entity.Playlist;
import com.example.shortudy.domain.playlist.entity.PlaylistVisibility;
import com.example.shortudy.domain.playlist.repository.PlaylistRepository;
import com.example.shortudy.domain.playlist.repository.PlaylistShortsRepository;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistShortsRepository playlistShortsRepository;
    private final ShortsRepository shortsRepository;
    private final UserRepository userRepository;


    public PlaylistService(
            PlaylistRepository playlistRepository,
            PlaylistShortsRepository playlistShortsRepository,
            ShortsRepository shortsRepository,
            UserRepository userRepository
    ) {
        this.playlistRepository = playlistRepository;
        this.playlistShortsRepository = playlistShortsRepository;
        this.shortsRepository = shortsRepository;
        this.userRepository = userRepository;
    }

    /**
     * 플레이리스트 생성
     * @param userId  플레이리스트를 만들 사용자 ID
     * @param request 생성 요청 DTO (제목, 설명, 공개여부)
     * @return 생성된 플레이리스트 정보
     */
    @Transactional
    public PlaylistResponse createPlaylist(Long userId, PlaylistCreateRequest request) {
        // 1. 사용자 조회 (없으면 예외 발생)
        if (userId == null) {
            throw new BaseException(ErrorCode.LOGIN_REQUIRED);
        }
        User user = findUserById(userId);

        // 2. 공개 여부 설정 (기본값: 비공개)
        // 삼항 연산자: request.visibility()가 null이 아니면 그 값, null이면 PRIVATE
        PlaylistVisibility visibility = request.visibility() != null
                ? request.visibility()
                : PlaylistVisibility.PRIVATE;

        // 3. 플레이리스트 엔티티 생성
        Playlist playlist = Playlist.create(
                user,
                request.title(),
                request.description(),
                visibility
        );

        // 4. 숏츠 추가 (요청에 shortsIds가 있으면)
        if (request.shortsIds() != null && !request.shortsIds().isEmpty()) {
            for (Long shortsId : request.shortsIds()) {
                Shorts shorts = findShortsById(shortsId);
                playlist.addShorts(shorts);
            }
        }

        // 5. 썸네일 숏츠 지정 (요청에 thumbnailShortsId가 있으면)
        //    - 지정하지 않으면 자동으로 첫 번째 숏츠의 썸네일 사용
        if (request.thumbnailShortsId() != null) {
            playlist.selectThumbnailFromShorts(request.thumbnailShortsId());
        }

        // 6. DB에 저장하고 응답 DTO로 변환하여 반환
        Playlist saved = playlistRepository.save(playlist);
        return PlaylistResponse.from(saved);
    }

    /**
     * 플레이리스트 상세 조회
     * [조회 권한 체크 로직]
     * - 공개 플레이리스트: 누구나 조회 가능
     * - 비공개 플레이리스트: 소유자만 조회 가능
     *
     * @param playlistId    조회할 플레이리스트 ID
     * @param currentUserId 현재 로그인한 사용자 ID (비로그인 시 null)
     * @return 플레이리스트 상세 정보 (담긴 숏츠 목록 포함)
     */
    public PlaylistDetailResponse getPlaylistDetail(Long playlistId, Long currentUserId) {
        // fetch join으로 관련 데이터를 한 번에 조회 (N+1 문제 방지)
        Playlist playlist = findPlaylistWithDetails(playlistId);

        // 권한 체크: 비공개이면서 소유자가 아니면 접근 거부
        if (!canAccess(playlist, currentUserId)) {
            throw new BaseException(ErrorCode.PLAYLIST_FORBIDDEN);
        }

        return PlaylistDetailResponse.from(playlist);
    }

    /**
     * 플레이리스트 수정
     * [더티 체킹(Dirty Checking)]
     * - JPA는 트랜잭션 내에서 엔티티 변경을 자동 감지
     * - playlist.update() 호출만으로 DB 업데이트 됨
     * - 별도로 save() 호출할 필요 없음!
     *
     * @param playlistId 수정할 플레이리스트 ID
     * @param userId     요청한 사용자 ID
     * @param request    수정 요청 DTO
     * @return 수정된 플레이리스트 정보
     */
    @Transactional
    public PlaylistResponse updatePlaylist(
            Long playlistId,
            Long userId,
            PlaylistUpdateRequest request
    ) {
        // 썸네일 숏츠 선택을 위해 숏츠 목록까지 함께 조회
        Playlist playlist = findPlaylistWithDetails(playlistId);
        validateOwnership(playlist, userId);  // 소유자 검증

        // 엔티티 수정 (더티 체킹으로 자동 UPDATE)
        playlist.update(request.title(), request.description(), request.visibility());

        // 썸네일 숏츠 지정: 플레이리스트 내 숏츠 중 선택
        if (request.thumbnailShortsId() != null) {
            if (request.thumbnailShortsId().equals(0L)) {
                // 0을 보내면 자동 썸네일로 초기화
                playlist.clearCustomThumbnail();
            } else {
                playlist.selectThumbnailFromShorts(request.thumbnailShortsId());
            }
        }

        return PlaylistResponse.from(playlist);
    }

    /**
     * 플레이리스트 삭제
     * [cascade 설정으로 인한 연쇄 삭제]
     * - Playlist 엔티티에 cascade = CascadeType.ALL 설정됨
     * - 플레이리스트 삭제 시 PlaylistShorts도 자동 삭제
     */
    @Transactional
    public void deletePlaylist(Long playlistId, Long userId) {
        Playlist playlist = findPlaylistWithUser(playlistId);
        validateOwnership(playlist, userId);

        playlistRepository.delete(playlist);
    }

    /**
     * 내 플레이리스트 목록 조회 (페이지네이션)
     * [메서드 레퍼런스 문법]
     * - .map(PlaylistResponse::from)
     * - .map(p -> PlaylistResponse.from(p)) 와 동일
     * - 각 Playlist를 PlaylistResponse로 변환
     */
    public Page<PlaylistResponse> getMyPlaylists(Long userId, Pageable pageable) {
        return playlistRepository.findByUserId(userId, pageable)
                .map(PlaylistResponse::from);
    }

    /**
     * 특정 사용자의 공개 플레이리스트 목록 조회
     */
    public Page<PlaylistResponse> getUserPublicPlaylists(Long targetUserId, Pageable pageable) {
        return playlistRepository.findByUserIdAndVisibility(
                targetUserId,
                PlaylistVisibility.PUBLIC,
                pageable
        ).map(PlaylistResponse::from);
    }

    /**
     * 전체 공개 플레이리스트 목록 조회
     */
    public Page<PlaylistResponse> getPublicPlaylists(Pageable pageable) {
        return playlistRepository.findByVisibility(PlaylistVisibility.PUBLIC, pageable)
                .map(PlaylistResponse::from);
    }

    public Page<PlaylistResponse> searchPublicPlaylists(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }
        return playlistRepository.searchByTitleAndVisibility(
                query,
                PlaylistVisibility.PUBLIC,
                pageable
        ).map(PlaylistResponse::from);
    }

    /**
     * 플레이리스트 아이템(숏츠) 목록 조회 (페이지네이션)
     * [상세 조회와의 차이점]
     * - getPlaylistDetail: 플레이리스트 정보 + 전체 숏츠 목록
     * - getPlaylistItems: 숏츠 목록만 페이지네이션으로 조회
     * - 숏츠가 많을 때 성능 최적화를 위해 사용
     */
    public Page<PlaylistDetailResponse.PlaylistShortsItem> getPlaylistItems(
            Long playlistId,
            Long currentUserId,
            Pageable pageable
    ) {
        Playlist playlist = findPlaylistWithUser(playlistId);

        // 권한 체크
        if (!canAccess(playlist, currentUserId)) {
            throw new BaseException(ErrorCode.PLAYLIST_FORBIDDEN);
        }

        // fetch join으로 숏츠와 작성자 정보를 함께 조회
        return playlistShortsRepository.findByPlaylistIdWithShorts(playlistId, pageable)
                .map(PlaylistDetailResponse.PlaylistShortsItem::from);
    }

    /**
     * 플레이리스트에 숏츠 추가
     * [비즈니스 로직 위치]
     * - 실제 추가 로직은 Playlist 엔티티의 addShorts() 메서드에 있음
     * - 도메인 주도 설계(DDD)에서는 비즈니스 로직을 엔티티에 두는 것을 권장
     * - Service는 흐름 제어와 트랜잭션 관리에 집중
     */
    @Transactional
    public PlaylistDetailResponse addShortsToPlaylist(
            Long playlistId,
            Long userId,
            PlaylistShortsAddRequest request
    ) {
        Playlist playlist = findPlaylistWithDetails(playlistId);
        validateOwnership(playlist, userId);

        Shorts shorts = findShortsById(request.shortsId());
        playlist.addShorts(shorts);  // 엔티티 메서드 호출

        return PlaylistDetailResponse.from(playlist);
    }

    /**
     * 플레이리스트에서 숏츠 제거
     */
    @Transactional
    public void removeShortsFromPlaylist(
            Long playlistId,
            Long userId,
            Long shortsId
    ) {
        Playlist playlist = findPlaylistWithDetails(playlistId);
        validateOwnership(playlist, userId);

        playlist.removeShorts(shortsId);  // 엔티티 메서드 호출
    }

    /**
     * 플레이리스트 내 숏츠 순서 변경
     */
    @Transactional
    public PlaylistDetailResponse reorderShorts(
            Long playlistId,
            Long userId,
            PlaylistShortsReorderRequest request
    ) {
        Playlist playlist = findPlaylistWithDetails(playlistId);
        validateOwnership(playlist, userId);

        playlist.reorderShorts(request.shortsId(), request.newIndex());  // 엔티티 메서드 호출

        return PlaylistDetailResponse.from(playlist);
    }

    // ========== private 헬퍼 메서드들 ==========

    /**
     * 사용자 ID로 User 엔티티 조회
     * [Optional과 orElseThrow]
     * - findById()는 Optional<User>를 반환
     * - Optional: 값이 있을 수도, 없을 수도 있음을 표현
     * - orElseThrow(): 값이 없으면 예외 발생, 있으면 값 반환
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 숏츠 ID로 Shorts 엔티티 조회
     */
    private Shorts findShortsById(Long shortsId) {
        return shortsRepository.findById(shortsId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));
    }

    /**
     * 플레이리스트 조회 (User만 fetch join)
     * - 소유자 확인만 필요할 때 사용
     */
    private Playlist findPlaylistWithUser(Long playlistId) {
        return playlistRepository.findWithUserById(playlistId)
                .orElseThrow(() -> new BaseException(ErrorCode.PLAYLIST_NOT_FOUND));
    }

    /**
     * 플레이리스트 조회 (User + PlaylistShorts + Shorts 모두 fetch join)
     * - 상세 정보가 필요할 때 사용
     * [N+1 문제와 fetch join]
     * - N+1 문제: 1번의 쿼리 후 연관 데이터 조회를 위해 N번 추가 쿼리 발생
     * - fetch join: 연관 데이터를 한 번의 쿼리로 함께 조회
     * - 예: 플레이리스트 1개 + 숏츠 10개 → 일반 조회 시 11번 쿼리, fetch join 시 1번 쿼리
     */
    private Playlist findPlaylistWithDetails(Long playlistId) {
        return playlistRepository.findWithDetailsById(playlistId)
                .orElseThrow(() -> new BaseException(ErrorCode.PLAYLIST_NOT_FOUND));
    }

    /**
     * 소유자 권한 검증
     * [Null Safety]
     * - userId가 null이면 로그인 필요 예외 발생
     * - 소유자가 아니면 권한 없음 예외 발생
     */
    private void validateOwnership(Playlist playlist, Long userId) {
        if (userId == null) {
            throw new BaseException(ErrorCode.LOGIN_REQUIRED);
        }
        if (!playlist.isOwner(userId)) {
            throw new BaseException(ErrorCode.PLAYLIST_FORBIDDEN);
        }
    }

    private boolean canAccess(Playlist playlist, Long currentUserId) {
        if (playlist.isPublic()) {
            return true;
        }
        return currentUserId != null && playlist.isOwner(currentUserId);
    }
}
