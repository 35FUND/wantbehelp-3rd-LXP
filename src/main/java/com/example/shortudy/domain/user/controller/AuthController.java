package com.example.shortudy.domain.user.controller;

import com.example.shortudy.domain.user.dto.request.TokenRefreshRequest;
import com.example.shortudy.domain.user.dto.request.UserLoginRequest;
import com.example.shortudy.domain.user.dto.request.UserSignUpRequest;
import com.example.shortudy.domain.user.dto.response.AuthStatusResponse;
import com.example.shortudy.domain.user.dto.response.UserLoginResponse;
import com.example.shortudy.domain.user.service.AuthService;
import com.example.shortudy.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 컨트롤러
 * - 토큰은 ResponseCookie로 설정 (sameSite 지원)
 * - 환경별로 sameSite, secure 설정 변경 가능
 */
@Tag(name = "Auth", description = "인증/인가 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    // 쿠키 설정
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int ACCESS_TOKEN_EXPIRY = 30 * 60; // 30분
    private static final int REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60; // 7일

    // 환경별 쿠키 설정 (application.properties에서 주입)
    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Valid UserSignUpRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. Access Token(30분)과 Refresh Token(7일)은 HTTP Only 쿠키로 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 오류)")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(
            @RequestBody @Valid UserLoginRequest request,
            HttpServletResponse httpResponse) {

        // 1. 서비스 로직 수행 (Access Token, Refresh Token, User 정보 받아옴)
        Map<String, Object> loginResult = authService.login(request);

        String accessToken = (String) loginResult.get("accessToken");
        String refreshToken = (String) loginResult.get("refreshToken");
        UserLoginResponse response = (UserLoginResponse) loginResult.get("response");

        // 2. Access Token을 ResponseCookie로 생성 (30분, sameSite 지원)
        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessToken)
                .httpOnly(true)       // XSS 공격 방지 (JavaScript 접근 불가)
                .secure(cookieSecure)  // HTTPS 전송 (로컬: false, 프로덕션: true)
                .path("/")            // 모든 경로에서 유효
                .maxAge(ACCESS_TOKEN_EXPIRY)  // 30분
                .sameSite(cookieSameSite)  // 로컬: Lax, 프로덕션: None (CORS 대응)
                .build();

        // 3. Refresh Token을 ResponseCookie로 생성 (7일, sameSite 지원)
        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)       // XSS 공격 방지
                .secure(cookieSecure)  // HTTPS 전송
                .path("/")            // 모든 경로에서 유효
                .maxAge(REFRESH_TOKEN_EXPIRY)  // 7일
                .sameSite(cookieSameSite)  // CORS 대응
                .build();

        // 4. 응답 헤더에 쿠키 심기
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃", description = "로그아웃합니다. Access Token과 Refresh Token 쿠키를 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse httpResponse) {
        // Access Token 쿠키 삭제
        ResponseCookie deleteAccessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)  // 즉시 삭제
                .sameSite(cookieSameSite)
                .build();

        // Refresh Token 쿠키 삭제
        ResponseCookie deleteRefreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)  // 즉시 삭제
                .sameSite(cookieSameSite)
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());

        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "로그인 상태 조회", description = "현재 로그인 상태를 확인합니다. (토큰 필요)")
    @GetMapping("/stat")
    public ResponseEntity<ApiResponse<AuthStatusResponse>> getAuthStatus(@AuthenticationPrincipal String email) {
        AuthStatusResponse authStatus = authService.getAuthStatus(email);
        return ResponseEntity.ok(ApiResponse.success(authStatus));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급받습니다. 새 토큰들은 HTTP Only 쿠키로 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UserLoginResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse httpResponse) {

        // 쿠키에서 Refresh Token 추출
        String refreshTokenFromCookie = extractRefreshTokenFromCookie(request);
        if (refreshTokenFromCookie == null) {
            throw new IllegalArgumentException("Refresh Token이 쿠키에 없습니다.");
        }

        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest(refreshTokenFromCookie);
        Map<String, Object> refreshResult = authService.refreshToken(tokenRefreshRequest);

        String newAccessToken = (String) refreshResult.get("accessToken");
        String newRefreshToken = (String) refreshResult.get("refreshToken");
        UserLoginResponse response = (UserLoginResponse) refreshResult.get("response");

        // 새로운 Access Token을 ResponseCookie로 생성
        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, newAccessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(ACCESS_TOKEN_EXPIRY)
                .sameSite(cookieSameSite)
                .build();

        // 새로운 Refresh Token을 ResponseCookie로 생성
        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, newRefreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRY)
                .sameSite(cookieSameSite)
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(response));
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
}

