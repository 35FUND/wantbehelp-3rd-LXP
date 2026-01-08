package com.example.shortudy.domain.user.controller;

import com.example.shortudy.global.security.principal.CustomUserDetails;
import com.example.shortudy.domain.user.dto.request.RefreshRequest;
import com.example.shortudy.domain.user.dto.request.LoginRequest;
import com.example.shortudy.domain.user.dto.response.LoginResponse;
import com.example.shortudy.domain.user.service.AuthService;
import com.example.shortudy.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    // 쿠키 설정 TODO 쿠키 만료 값과 토큰 만료 값의 오차
//    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
//    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
//    private static final int ACCESS_TOKEN_EXPIRY = 30 * 60; // 30분
//    private static final int REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60; // 7일

    // 환경별 쿠키 설정 (application.properties에서 주입)
//    @Value("${app.cookie.same-site:Lax}")
//    private String cookieSameSite;
//
//    @Value("${app.cookie.secure:false}")
//    private boolean cookieSecure;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {

        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestBody @Valid RefreshRequest request) {

        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request.refreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {

        authService.logout(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

//    @PostMapping("/login")
//    public ResponseEntity<ApiResponse<LoginResponse>> login(
//            @RequestBody @Valid LoginRequest request,
//            HttpServletResponse httpResponse) {
//
//        // 1. 서비스 로직 수행 (Access Token, Refresh Token, User 정보 받아옴)
//        Map<String, Object> loginResult = authService.login(request);
//
//        String accessToken = (String) loginResult.get("accessToken");
//        String refreshToken = (String) loginResult.get("refreshToken");
//        LoginResponse response = (LoginResponse) loginResult.get("response");
//
//        // 2. Access Token을 ResponseCookie로 생성 (30분, sameSite 지원)
//        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessToken)
//                .httpOnly(true)       // XSS 공격 방지 (JavaScript 접근 불가)
//                .secure(cookieSecure)  // HTTPS 전송 (로컬: false, 프로덕션: true)
//                .path("/")            // 모든 경로에서 유효
//                .maxAge(ACCESS_TOKEN_EXPIRY)  // 30분
//                .sameSite(cookieSameSite)  // 로컬: Lax, 프로덕션: None (CORS 대응)
//                .build();
//
//        // 3. Refresh Token을 ResponseCookie로 생성 (7일, sameSite 지원)
//        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
//                .httpOnly(true)       // XSS 공격 방지
//                .secure(cookieSecure)  // HTTPS 전송
//                .path("/")            // 모든 경로에서 유효
//                .maxAge(REFRESH_TOKEN_EXPIRY)  // 7일
//                .sameSite(cookieSameSite)  // CORS 대응
//                .build();
//
//        // 4. 응답 헤더에 쿠키 심기
//        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
//        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
//
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse httpResponse) {
//        // Access Token 쿠키 삭제
//        ResponseCookie deleteAccessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
//                .httpOnly(true)
//                .secure(cookieSecure)
//                .path("/")
//                .maxAge(0)  // 즉시 삭제
//                .sameSite(cookieSameSite)
//                .build();
//
//        // Refresh Token 쿠키 삭제
//        ResponseCookie deleteRefreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
//                .httpOnly(true)
//                .secure(cookieSecure)
//                .path("/")
//                .maxAge(0)  // 즉시 삭제
//                .sameSite(cookieSameSite)
//                .build();
//
//        httpResponse.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());
//        httpResponse.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());
//
//        return ResponseEntity.ok(ApiResponse.success());
//    }
//
//    @GetMapping("/stat")
//    public ResponseEntity<ApiResponse<AuthStatusResponse>> getAuthStatus(@AuthenticationPrincipal String email) {
//        AuthStatusResponse authStatus = authService.getAuthStatus(email);
//        return ResponseEntity.ok(ApiResponse.success(authStatus));
//    }


//    @PostMapping("/refresh")
//    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
//            HttpServletRequest request,
//            HttpServletResponse httpResponse) {
//
//        // 쿠키에서 Refresh Token 추출
//        String refreshTokenFromCookie = extractRefreshTokenFromCookie(request);
//        if (refreshTokenFromCookie == null) {
//            throw new IllegalArgumentException("Refresh Token이 쿠키에 없습니다.");
//        }
//
//        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest(refreshTokenFromCookie);
//        Map<String, Object> refreshResult = authService.refreshToken(tokenRefreshRequest);
//
//        String newAccessToken = (String) refreshResult.get("accessToken");
//        String newRefreshToken = (String) refreshResult.get("refreshToken");
//        LoginResponse response = (LoginResponse) refreshResult.get("response");
//
//        // 새로운 Access Token을 ResponseCookie로 생성
//        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, newAccessToken)
//                .httpOnly(true)
//                .secure(cookieSecure)
//                .path("/")
//                .maxAge(ACCESS_TOKEN_EXPIRY)
//                .sameSite(cookieSameSite)
//                .build();
//
//        // 새로운 Refresh Token을 ResponseCookie로 생성
//        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, newRefreshToken)
//                .httpOnly(true)
//                .secure(cookieSecure)
//                .path("/")
//                .maxAge(REFRESH_TOKEN_EXPIRY)
//                .sameSite(cookieSameSite)
//                .build();
//
//        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
//        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
//
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }

    /**
     * 쿠키에서 Refresh Token 추출
     */
//    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
//                    return cookie.getValue();
//                }
//            }
//        }
//        return null;
//    }
}

