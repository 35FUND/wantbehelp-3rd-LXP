package com.example.shortudy.domain.playlist.playlist.controller;

import com.example.shortudy.domain.playlist.dto.request.PlaylistCreateRequest;
import com.example.shortudy.domain.playlist.dto.request.PlaylistShortsAddRequest;
import com.example.shortudy.domain.playlist.dto.request.PlaylistShortsReorderRequest;
import com.example.shortudy.domain.playlist.dto.request.PlaylistUpdateRequest;
import com.example.shortudy.domain.playlist.dto.response.PlaylistDetailResponse;
import com.example.shortudy.domain.playlist.dto.response.PlaylistResponse;
import com.example.shortudy.domain.playlist.service.PlaylistService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/playlists")
public class PlaylistController {

    //
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PlaylistResponse> createPlaylist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PlaylistCreateRequest request
    ) {
        PlaylistResponse response = playlistService.createPlaylist(
                userDetails.getId(),
                request
        );
        return ApiResponse.success(response);
    }

    @GetMapping("/{playlistId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PlaylistDetailResponse> getPlaylistDetail(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 삼항 연산자: 조건 ? 참일때값 : 거짓일때값
        Long currentUserId = userDetails != null ? userDetails.getId() : null;
        PlaylistDetailResponse response = playlistService.getPlaylistDetail(
                playlistId,
                currentUserId
        );
        return ApiResponse.success(response);
    }

    /**
     * 플레이리스트 수정 API
     * [HTTP 메서드: PATCH]
     * - 리소스의 일부만 수정할 때 사용
     * - PUT은 전체 교체, PATCH는 부분 수정
     * - 예: 제목만 바꾸고 싶으면 title만 보내면 됨
     */
    @PatchMapping("/{playlistId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PlaylistResponse> updatePlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PlaylistUpdateRequest request
    ) {
        PlaylistResponse response = playlistService.updatePlaylist(
                playlistId,
                userDetails.getId(),
                request
        );
        return ApiResponse.success(response);
    }

    /**
     * 플레이리스트 삭제 API
     * [HTTP 메서드: DELETE]
     * - 리소스를 삭제할 때 사용
     * [@ResponseStatus(HttpStatus.NO_CONTENT)]
     * - 204 No Content 상태 코드 반환
     * - 삭제 성공했지만 반환할 데이터가 없음을 의미
     */
    @DeleteMapping("/{playlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deletePlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        playlistService.deletePlaylist(playlistId, userDetails.getId());
        return ApiResponse.success(null);
    }

    /**
     * 내 플레이리스트 목록 조회 API
     * [페이지네이션(Pagination)]
     * - 많은 데이터를 한 번에 가져오면 성능 문제 발생
     * - 페이지 단위로 나눠서 조회 (예: 1페이지에 20개씩)
     * [@PageableDefault]
     * - 페이지 기본값 설정
     * - size = 10: 한 페이지에 10개
     * - sort = "createdAt": 생성일 기준 정렬
     * - direction = DESC: 내림차순 (최신순)
     */
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<PlaylistResponse>> getMyPlaylists(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        Page<PlaylistResponse> response = playlistService.getMyPlaylists(
                userDetails.getId(),
                pageable
        );
        return ApiResponse.success(response);
    }



    /**
     * 전체 공개 플레이리스트 목록 조회 API
     * - 로그인 없이도 조회 가능
     * - 모든 사용자의 공개 플레이리스트를 최신순으로 조회
     */
    @GetMapping("/public")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<PlaylistResponse>> getPublicPlaylists(
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        Page<PlaylistResponse> response = playlistService.getPublicPlaylists(pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<PlaylistResponse>> searchPublicPlaylists(
            @RequestParam String query,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        Page<PlaylistResponse> response = playlistService.searchPublicPlaylists(query, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 플레이리스트에 숏츠 추가 API
     * [URL 설계]
     * - POST /api/v1/playlists/{playlistId}/items
     *  - RESTful한 URL 설계 패턴
     */
    @PostMapping("/{playlistId}/items")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PlaylistDetailResponse> addShortsToPlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PlaylistShortsAddRequest request
    ) {
        PlaylistDetailResponse response = playlistService.addShortsToPlaylist(
                playlistId,
                userDetails.getId(),
                request
        );
        return ApiResponse.success(response);
    }

    /**
     * 플레이리스트에서 숏츠 제거 API
     * [URL 설계]
     * - DELETE /api/v1/playlists/{playlistId}/items/{shortsId}
     * - "플레이리스트의 아이템 중 특정 숏츠를 제거한다"
     */
    @DeleteMapping("/{playlistId}/items/{shortsId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeShortsFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long shortsId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        playlistService.removeShortsFromPlaylist(
                playlistId,
                userDetails.getId(),
                shortsId
        );
        return ApiResponse.success(null);
    }

    /**
     * 플레이리스트 아이템(숏츠) 목록 조회 API
     * - 플레이리스트에 담긴 숏츠들을 페이지네이션으로 조회
     * - position(순서) 기준 정렬
     */
    @GetMapping("/{playlistId}/items")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<PlaylistDetailResponse.PlaylistShortsItem>> getPlaylistItems(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "position") Pageable pageable
    ) {
        Long currentUserId = userDetails != null ? userDetails.getId() : null;
        Page<PlaylistDetailResponse.PlaylistShortsItem> response = playlistService.getPlaylistItems(
                playlistId,
                currentUserId,
                pageable
        );
        return ApiResponse.success(response);
    }

    /**
     * 플레이리스트 내 숏츠 순서 변경 API
     * [PATCH vs PUT]
     * - 순서만 변경하는 것이므로 PATCH 사용
     * - 전체 목록을 교체하는 게 아니라 특정 항목의 위치만 변경
     */
    @PatchMapping("/{playlistId}/items/reorder")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PlaylistDetailResponse> reorderShorts(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PlaylistShortsReorderRequest request
    ) {
        PlaylistDetailResponse response = playlistService.reorderShorts(
                playlistId,
                userDetails.getId(),
                request
        );
        return ApiResponse.success(response);
    }
}
