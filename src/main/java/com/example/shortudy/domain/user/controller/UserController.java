package com.example.shortudy.domain.user.controller;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.user.dto.UserResponse;
import com.example.shortudy.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다. (토큰 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 숏폼 목록 조회", description = "로그인한 사용자가 업로드한 숏폼 목록을 조회합니다. (토큰 필요)")
    @GetMapping("/me/shorts")
    public ResponseEntity<Page<ShortsResponse>> getMyShorts(
            @AuthenticationPrincipal String email,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 8) Pageable pageable) {
        Page<ShortsResponse> response = userService.getMyShorts(email, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전체 사용자 조회", description = "모든 사용자 목록을 조회합니다. (관리자용)")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 조회", description = "특정 사용자 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다. (관리자용)")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}

