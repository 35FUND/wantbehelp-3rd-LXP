package com.example.shortudy.domain.user.controller;

import com.example.shortudy.domain.user.dto.request.TokenRefreshRequest;
import com.example.shortudy.domain.user.dto.request.UserLoginRequest;
import com.example.shortudy.domain.user.dto.request.UserSignUpRequest;
import com.example.shortudy.domain.user.dto.response.AuthStatusResponse;
import com.example.shortudy.domain.user.dto.response.UserLoginResponse;
import com.example.shortudy.domain.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증/인가 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid UserSignUpRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. Access Token과 Refresh Token을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 오류)")
    })
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        UserLoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃", description = "로그아웃합니다. (클라이언트에서 토큰 삭제)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal String email) {
        authService.logout(email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인 상태 조회", description = "현재 로그인 상태를 확인합니다. (토큰 필요)")
    @GetMapping("/stat")
    public ResponseEntity<AuthStatusResponse> getAuthStatus(@AuthenticationPrincipal String email) {
        AuthStatusResponse response = authService.getAuthStatus(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<UserLoginResponse> refreshToken(@RequestBody @Valid TokenRefreshRequest request) {
        UserLoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}