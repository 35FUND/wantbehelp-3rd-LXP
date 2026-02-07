package com.example.shortudy.domain.like.Controller;

import com.example.shortudy.domain.like.dto.LikeToggleResponse;
import com.example.shortudy.domain.like.dto.MyLikedShortsResponse;
import com.example.shortudy.domain.like.service.ShortsLikeService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 좋아요 엔드포인트
 */
@RestController
@RequestMapping("/api/v1")
public class ShortsLikeController {

    private final ShortsLikeService shortsLikeService;

    public ShortsLikeController(ShortsLikeService shortsLikeService) {
        this.shortsLikeService = shortsLikeService;
    }

    /**
     * [POST] 좋아요 요청
     * @param me 로그인 된 유저 정보
     * @param shortsId 숏츠 ID
     * @return 좋아요 상태 DTO
     */
    @PostMapping("/shorts/{shortsId}/likes")
    public ResponseEntity<ApiResponse<LikeToggleResponse>> like(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long shortsId
    ) {
        LikeToggleResponse response = shortsLikeService.toggleLike(me.getId(), shortsId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @GetMapping("/me/likes/shorts")
    public ResponseEntity<ApiResponse<MyLikedShortsResponse>> getMyLikeShorts(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            Pageable pageable
    ) {
        MyLikedShortsResponse response = shortsLikeService.getMyLikedShorts(me.getId(), sort, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @Deprecated(since = "토글 형태로 바꿀 예정이므로 삭제 예정")
    @DeleteMapping("/shorts/{shortsId}/unlikes")
    public ResponseEntity<ApiResponse<Void>> unlike(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long shortsId
    ) {
        shortsLikeService.unlike(me.getId(), shortsId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }
}
