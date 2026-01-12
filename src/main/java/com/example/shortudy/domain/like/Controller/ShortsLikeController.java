package com.example.shortudy.domain.like.Controller;

import com.example.shortudy.domain.like.entity.ShortsLike;
import com.example.shortudy.domain.like.service.ShortsLikeService;
import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
