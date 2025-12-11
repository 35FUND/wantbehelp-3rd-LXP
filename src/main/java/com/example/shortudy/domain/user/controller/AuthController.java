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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증/인가 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60; // 7일

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

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. Access Token은 응답 본문, Refresh Token은 HTTP Only 쿠키로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 오류)")
    })
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @RequestBody @Valid UserLoginRequest request,
            HttpServletResponse response) {
        UserLoginResponse loginResponse = authService.login(request);

        // Refresh Token을 HTTP Only 쿠키에 저장
        setRefreshTokenCookie(response, loginResponse.refreshToken());

        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "로그아웃", description = "로그아웃합니다. Refresh Token 쿠키를 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Refresh Token 쿠키 삭제
        deleteRefreshTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인 상태 조회", description = "현재 로그인 상태를 확인합니다. (토큰 필요)")
    @GetMapping("/stat")
    public ResponseEntity<AuthStatusResponse> getAuthStatus(@AuthenticationPrincipal String email) {
        AuthStatusResponse authStatus = authService.getAuthStatus(email);
        return ResponseEntity.ok(authStatus);
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급받습니다. 새 Refresh Token은 HTTP Only 쿠키로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<UserLoginResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        // 쿠키에서 Refresh Token 추출
        String refreshTokenFromCookie = extractRefreshTokenFromCookie(request);
        if (refreshTokenFromCookie == null) {
            throw new IllegalArgumentException("Refresh Token이 쿠키에 없습니다.");
        }

        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest(refreshTokenFromCookie);
        UserLoginResponse refreshResponse = authService.refreshToken(tokenRefreshRequest);

        // 새로운 Refresh Token을 HTTP Only 쿠키에 저장
        setRefreshTokenCookie(response, refreshResponse.refreshToken());

        return ResponseEntity.ok(refreshResponse);
    }

    /**
     * 쿠키에서 Refresh Token 추출
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Refresh Token을 HTTP Only 쿠키에 저장
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);           // JavaScript에서 접근 불가 (보안)
        cookie.setSecure(false);            // HTTPS에서만 전송 (개발 환경이므로 false, 프로덕션에서는 true)
        cookie.setPath("/");                // 모든 경로에서 유효
        cookie.setMaxAge(REFRESH_TOKEN_EXPIRY);  // 7일
        cookie.setSameSite("Strict");       // CSRF 방지
        response.addCookie(cookie);
    }

    /**
     * Refresh Token 쿠키 삭제
     */
    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 쿠키 삭제
        response.addCookie(cookie);
    }
}