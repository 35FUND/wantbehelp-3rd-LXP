package com.example.shortudy.domain.playlist.service;

import com.example.shortudy.domain.comment.repository.CommentRepository;
import com.example.shortudy.domain.like.repository.ShortsLikeRepository;
import com.example.shortudy.domain.playlist.dto.request.PlaylistCreateRequest;
import com.example.shortudy.domain.playlist.dto.request.PlaylistShortsAddRequest;
import com.example.shortudy.domain.playlist.dto.request.PlaylistShortsReorderRequest;
import com.example.shortudy.domain.playlist.dto.request.PlaylistUpdateRequest;
import com.example.shortudy.domain.playlist.dto.response.OwnerInfo;
import com.example.shortudy.domain.playlist.dto.response.PlaylistDetailResponse;
import com.example.shortudy.domain.playlist.dto.response.PlaylistResponse;
import com.example.shortudy.domain.playlist.entity.Playlist;
import com.example.shortudy.domain.playlist.entity.PlaylistShorts;
import com.example.shortudy.domain.playlist.entity.PlaylistVisibility;
import com.example.shortudy.domain.playlist.repository.PlaylistRepository;
import com.example.shortudy.domain.playlist.repository.PlaylistShortsRepository;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.config.S3Service;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistShortsRepository playlistShortsRepository;
    private final ShortsRepository shortsRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ShortsLikeRepository shortsLikeRepository;
    private final S3Service s3Service;


    public PlaylistService(
            PlaylistRepository playlistRepository,
            PlaylistShortsRepository playlistShortsRepository,
            ShortsRepository shortsRepository,
            UserRepository userRepository,
            CommentRepository commentRepository,
            ShortsLikeRepository shortsLikeRepository,
            S3Service s3Service
    ) {
        this.playlistRepository = playlistRepository;
        this.playlistShortsRepository = playlistShortsRepository;
        this.shortsRepository = shortsRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.shortsLikeRepository = shortsLikeRepository;
        this.s3Service = s3Service;
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

        // 2. 공개 여부 설정 (항상 공개로 고정)
        PlaylistVisibility visibility = PlaylistVisibility.PUBLIC;

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
        return convertProfileUrl(PlaylistResponse.from(saved));
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

        return buildDetailResponse(playlist, currentUserId);
    }

    /**
     * 플레이리스트 수정
     * [더티 체킹(Dirty Checking)]
     * - JPA는 트랜잭션 내에서 엔티티 변경을 자동 감지
     * - playlist.update() 호출만으로 DB 업데이트 됨
     * - 별도로 save() 호출할 필요 없음!
     * [조회 최적화]
     * - 썸네일 변경이 없으면 User만 fetch join (가벼운 쿼리)
     * - 썸네일 변경이 있으면 전체 fetch join (숏츠 목록 필요)
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
        boolean needsThumbnailChange = request.thumbnailShortsId() != null;

        // 썸네일 변경이 필요하면 숏츠 목록까지, 아니면 User만 조회
        Playlist playlist = needsThumbnailChange
                ? findPlaylistWithDetails(playlistId)
                : findPlaylistWithUser(playlistId);
        validateOwnership(playlist, userId);

        // 엔티티 수정 (더티 체킹으로 자동 UPDATE)
        playlist.update(request.title(), request.description(), request.visibility());

        // 썸네일 숏츠 지정: 플레이리스트 내 숏츠 중 선택
        if (needsThumbnailChange) {
            if (request.thumbnailShortsId().equals(0L)) {
                // 0을 보내면 자동 썸네일로 초기화
                playlist.clearCustomThumbnail();
            } else {
                playlist.selectThumbnailFromShorts(request.thumbnailShortsId());
            }
        }

        return convertProfileUrl(PlaylistResponse.from(playlist));
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
                .map(PlaylistResponse::from)
                .map(this::convertProfileUrl);
    }

    /**
     * 특정 사용자의 공개 플레이리스트 목록 조회
     */
    public Page<PlaylistResponse> getUserPublicPlaylists(Long targetUserId, Pageable pageable) {
        return playlistRepository.findByUserId(targetUserId, pageable)
                .map(PlaylistResponse::from)
                .map(this::convertProfileUrl);
    }

    /**
     * 전체 공개 플레이리스트 목록 조회
     */
    public Page<PlaylistResponse> getPublicPlaylists(Pageable pageable) {
        return playlistRepository.findByVisibility(PlaylistVisibility.PUBLIC, pageable)
                .map(PlaylistResponse::from)
                .map(this::convertProfileUrl);
    }

    public Page<PlaylistResponse> searchPublicPlaylists(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }
        return playlistRepository.searchByTitleAndVisibility(
                query,
                PlaylistVisibility.PUBLIC,
                pageable
        ).map(PlaylistResponse::from)
        .map(this::convertProfileUrl);
    }

    /**
     * 플레이리스트 아이템(숏츠) 목록 조회 (페이지네이션)
     * [상세 조회와의 차이점]
     * - getPlaylistDetail: 플레이리스트 정보 + 전체 숏츠 목록
     * - getPlaylistItems: 숏츠 목록만 페이지네이션으로 조회
     * - 숏츠가 많을 때 성능 최적화를 위해 사용
     * [2단계 쿼리 전략]
     * - 1단계: ID만 페이징 조회 (DB 페이징, 메모리 페이징 없음)
     * - 2단계: ID 목록으로 fetch join 조회 (N+1 방지)
     * - fetch join + Pageable 동시 사용 시 발생하는 HHH90003004 경고 해결
     */
    public Page<PlaylistDetailResponse.PlaylistShortsItem> getPlaylistItems(
            Long playlistId,
            Long currentUserId,
            Pageable pageable
    ) {
        Playlist playlist = findPlaylistWithUser(playlistId);

        // 1단계: ID만 페이징 조회
        Page<Long> idPage = playlistShortsRepository.findIdsByPlaylistId(playlistId, pageable);

        if (idPage.isEmpty()) {
            return idPage.map(id -> null);
        }

        // 2단계: ID 목록으로 fetch join 조회
        List<PlaylistShorts> items = playlistShortsRepository.findByIdsWithShorts(idPage.getContent());

        // 키워드 초기화 (LAZY 컬렉션)
        initializeKeywords(items);

        // 숏츠 ID 목록 추출
        List<Long> shortsIds = items.stream()
                .map(ps -> ps.getShorts().getId())
                .toList();

        // 댓글 수, 좋아요 여부 배치 조회
        Map<Long, Long> commentCounts = getCommentCounts(shortsIds);
        Set<Long> likedShortsIds = getLikedShortsIds(currentUserId, shortsIds);

        // Page 구조를 유지하면서 DTO 변환
        return idPage.map(id -> items.stream()
                .filter(ps -> ps.getId().equals(id))
                .findFirst()
                .map(ps -> {
                    PlaylistDetailResponse.PlaylistShortsItem dto =
                            PlaylistDetailResponse.PlaylistShortsItem.from(ps, commentCounts, likedShortsIds);
                    return convertProfileUrl(dto);
                })
                .orElse(null));
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

        return buildDetailResponse(playlist, userId);
    }

    /**
     * 플레이리스트에서 숏츠 제거
     * [벌크 연산 적용]
     * - 기존: 엔티티에서 삭제 후 뒤쪽 아이템들을 하나씩 position -1 (N번 UPDATE)
     * - 개선: 삭제 후 벌크 쿼리로 한 번에 position 재정렬 (1번 UPDATE)
     */
    @Transactional
    public void removeShortsFromPlaylist(
            Long playlistId,
            Long userId,
            Long shortsId
    ) {
        Playlist playlist = findPlaylistWithUser(playlistId);
        validateOwnership(playlist, userId);

        // 1. 삭제 대상 조회
        PlaylistShorts target = playlistShortsRepository
                .findByPlaylistIdAndShortsId(playlistId, shortsId)
                .orElseThrow(() -> new BaseException(ErrorCode.PLAYLIST_ITEM_NOT_FOUND));

        int removedPosition = target.getPosition();

        // 2. 삭제
        playlistShortsRepository.delete(target);
        playlistShortsRepository.flush();

        // 3. 삭제된 위치 뒤의 항목들 position 일괄 감소 (벌크 연산)
        playlistShortsRepository.bulkDecrementPositionAfter(playlistId, removedPosition);

        // 4. 자동 썸네일 갱신 — 벌크 연산 후 영속성 컨텍스트가 초기화되므로 다시 조회
        Playlist refreshedPlaylist = findPlaylistWithDetails(playlistId);
        refreshThumbnailIfAuto(refreshedPlaylist);
    }

    /**
     * 플레이리스트 내 숏츠 순서 변경
     * [벌크 연산 적용]
     * - 기존: 엔티티에서 사이 항목들을 하나씩 position ±1 (N번 UPDATE)
     * - 개선: 벌크 쿼리로 한 번에 범위 내 position 업데이트 (2번 UPDATE: 범위 시프트 + 대상 이동)
     */
    @Transactional
    public PlaylistDetailResponse reorderShorts(
            Long playlistId,
            Long userId,
            PlaylistShortsReorderRequest request
    ) {
        Playlist playlist = findPlaylistWithUser(playlistId);
        validateOwnership(playlist, userId);

        int newIndex = request.newIndex();
        Long shortsId = request.shortsId();

        // 1. 대상 아이템 조회
        PlaylistShorts target = playlistShortsRepository
                .findByPlaylistIdAndShortsId(playlistId, shortsId)
                .orElseThrow(() -> new BaseException(ErrorCode.PLAYLIST_ITEM_NOT_FOUND));

        int currentPosition = target.getPosition();

        // 2. 유효 범위 검증
        int maxPosition = playlistShortsRepository.findMaxPositionByPlaylistId(playlistId);
        if (newIndex < 0 || newIndex > maxPosition) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }

        // 3. 같은 위치면 아무것도 안 함
        if (currentPosition == newIndex) {
            Playlist refreshed = findPlaylistWithDetails(playlistId);
            return buildDetailResponse(refreshed, userId);
        }

        // 4. 벌크 연산으로 사이 항목들 position 일괄 시프트
        if (currentPosition < newIndex) {
            // 아래로 이동: 사이 항목들 position -1
            playlistShortsRepository.bulkDecrementPositionBetween(playlistId, currentPosition, newIndex);
        } else {
            // 위로 이동: 사이 항목들 position +1
            playlistShortsRepository.bulkIncrementPositionBetween(playlistId, newIndex, currentPosition);
        }

        // 5. 대상 아이템의 position을 새 위치로 벌크 업데이트
        playlistShortsRepository.bulkUpdatePosition(playlistId, shortsId, newIndex);

        // 6. 자동 썸네일 갱신
        Playlist refreshedPlaylist = findPlaylistWithDetails(playlistId);
        refreshThumbnailIfAuto(refreshedPlaylist);

        return buildDetailResponse(refreshedPlaylist, userId);
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
     * 플레이리스트 조회 (User + PlaylistShorts + Shorts + Category 모두 fetch join)
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

    /**
     * 자동 썸네일 갱신 (서비스 레이어용)
     * - 커스텀 썸네일이 아닌 경우, 첫 번째 숏츠의 썸네일로 갱신
     * - 벌크 연산 후 영속성 컨텍스트가 초기화되므로 서비스에서 직접 처리
     */
    private void refreshThumbnailIfAuto(Playlist playlist) {
        if (!playlist.isThumbnailCustom()) {
            playlist.clearCustomThumbnail();
        }
    }

    // ========== 상세 응답 빌드 헬퍼 메서드들 ==========

    /**
     * PlaylistDetailResponse 빌드 헬퍼
     * - 키워드 초기화, 댓글 수 배치 조회, 좋아요 배치 조회를 수행 후 DTO 변환
     *
     * @param playlist      Playlist 엔티티 (fetch join으로 조회된 상태)
     * @param currentUserId 현재 로그인 사용자 ID (비로그인 시 null)
     * @return PlaylistDetailResponse DTO
     */
    private PlaylistDetailResponse buildDetailResponse(Playlist playlist, Long currentUserId) {
        List<PlaylistShorts> playlistShorts = playlist.getPlaylistShorts();

        // 키워드 LAZY 컬렉션 초기화 (fetch join 불가 — MultipleBagFetchException 방지)
        initializeKeywords(playlistShorts);

        // 숏츠 ID 목록 추출
        List<Long> shortsIds = playlistShorts.stream()
                .map(ps -> ps.getShorts().getId())
                .toList();

        // 댓글 수, 좋아요 여부 배치 조회
        Map<Long, Long> commentCounts = getCommentCounts(shortsIds);
        Set<Long> likedShortsIds = getLikedShortsIds(currentUserId, shortsIds);

        return convertProfileUrl(PlaylistDetailResponse.from(playlist, commentCounts, likedShortsIds));
    }

    /**
     * 키워드 LAZY 컬렉션 초기화
     * [Hibernate.initialize 사용 이유]
     * - shortsKeywords는 LAZY 로딩으로 설정됨
     * - fetch join으로 가져올 수 없음 (이미 playlistShorts → shorts 컬렉션 fetch join 중)
     * - 다중 컬렉션 fetch join은 MultipleBagFetchException 발생
     * - 따라서 Hibernate.initialize()로 개별 초기화
     */
    private void initializeKeywords(List<PlaylistShorts> playlistShorts) {
        for (PlaylistShorts ps : playlistShorts) {
            Hibernate.initialize(ps.getShorts().getShortsKeywords());
        }
    }

    /**
     * 숏츠별 댓글 수 배치 조회
     * - N+1 방지: 숏츠 개수만큼 COUNT 쿼리 실행 대신, 한 번의 쿼리로 모두 조회
     *
     * @param shortsIds 조회 대상 숏츠 ID 목록
     * @return 숏츠 ID → 댓글 수 맵
     */
    private Map<Long, Long> getCommentCounts(List<Long> shortsIds) {
        if (shortsIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentRepository.countAllCommentsByShortsIds(shortsIds).stream()
                .collect(Collectors.toMap(
                        p -> p.getShortsId(),
                        p -> p.getCnt()
                ));
    }

    /**
     * 현재 사용자가 좋아요한 숏츠 ID Set 조회
     * - 비로그인 사용자는 빈 Set 반환
     * - N+1 방지: 한 번의 쿼리로 모든 좋아요 상태 확인
     *
     * @param currentUserId 현재 사용자 ID (null이면 비로그인)
     * @param shortsIds     조회 대상 숏츠 ID 목록
     * @return 좋아요한 숏츠 ID Set
     */
    private Set<Long> getLikedShortsIds(Long currentUserId, List<Long> shortsIds) {
        if (currentUserId == null || shortsIds.isEmpty()) {
            return Collections.emptySet();
        }
        return shortsLikeRepository.findByUserIdAndShortsIdIn(currentUserId, shortsIds).stream()
                .map(like -> like.getShorts().getId())
                .collect(Collectors.toSet());
    }


    // 유저 프로필 URL S3 매핑해주기
    private PlaylistResponse convertProfileUrl(PlaylistResponse response) {
        String convertedUrl = s3Service.getFileUrl(response.owner().profileUrl());
        return new PlaylistResponse(
                response.id(),
                response.title(),
                response.description(),
                response.visibility(),
                response.thumbnailUrl(),
                response.thumbnailCustom(),
                response.shortsCount(),
                new OwnerInfo(
                        response.owner().id(),
                        response.owner().nickname(),
                        convertedUrl
                ),
                response.createdAt(),
                response.updatedAt()
        );
    }

    private PlaylistDetailResponse convertProfileUrl(PlaylistDetailResponse response) {
        // owner.profileUrl null-safe 변환
        String ownerProfile = response.owner() != null ? response.owner().profileUrl() : null;
        String convertedOwnerUrl = ownerProfile == null ? null : s3Service.getFileUrl(ownerProfile);

        // items 내부의 uploader.profileUrl도 변환 (null-safe)
        List<PlaylistDetailResponse.PlaylistShortsItem> convertedItems = null;
        if (response.items() != null) {
            convertedItems = response.items().stream()
                    .map(this::convertProfileUrl)
                    .toList();
        }

        return new PlaylistDetailResponse(
                response.id(),
                response.title(),
                response.description(),
                response.visibility(),
                response.thumbnailUrl(),
                response.thumbnailCustom(),
                response.shortsCount(),
                new OwnerInfo(
                        response.owner() != null ? response.owner().id() : null,
                        response.owner() != null ? response.owner().nickname() : null,
                        convertedOwnerUrl
                ),
                convertedItems,
                response.createdAt(),
                response.updatedAt()
        );
    }

    // PlaylistShortsItem 내부의 업로더 프로필 URL을 S3 URL로 변환
    private PlaylistDetailResponse.PlaylistShortsItem convertProfileUrl(PlaylistDetailResponse.PlaylistShortsItem item) {
        if (item == null) return null;

        PlaylistDetailResponse.ShortsInfo s = item.shorts();
        PlaylistDetailResponse.UploaderInfo uploader = s.uploader();

        String converted = s3Service.getFileUrl(uploader.profileUrl());

        PlaylistDetailResponse.UploaderInfo newUploader = new PlaylistDetailResponse.UploaderInfo(
                uploader.id(),
                uploader.nickname(),
                converted
        );

        PlaylistDetailResponse.ShortsInfo newShorts = new PlaylistDetailResponse.ShortsInfo(
                s.shortsId(),
                s.title(),
                s.description(),
                s.videoUrl(),
                s.thumbnailUrl(),
                s.durationSec(),
                s.status(),
                newUploader,
                s.category(),
                s.keywords(),
                s.viewCount(),
                s.likeCount(),
                s.commentCount(),
                s.createdAt(),
                s.updatedAt(),
                s.isLiked(),
                s.visibility()
        );

        return new PlaylistDetailResponse.PlaylistShortsItem(
                item.itemId(),
                item.position(),
                newShorts,
                item.addedAt()
        );
    }
}
