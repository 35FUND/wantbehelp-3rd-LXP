package com.example.shortudy.domain.like.Controller;

import com.example.shortudy.domain.like.service.ShortsLikeService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ShortsLikeController {

    private final ShortsLikeService shortsLikeService;

    public ShortsLikeController(ShortsLikeService shortsLikeService) {
        this.shortsLikeService = shortsLikeService;
    }

    @PostMapping("/shorts/{shortsId}/likes")
    public ResponseEntity<ApiResponse<Void>> like(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long shortsId
    ) {
        shortsLikeService.like(me.getId(), shortsId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @DeleteMapping("/shorts/{shortsId}/unlikes")
    public ResponseEntity<ApiResponse<Void>> unlike(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long shortsId
    ) {
        shortsLikeService.unlike(me.getId(), shortsId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }
}
