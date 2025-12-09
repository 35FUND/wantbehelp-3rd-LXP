package com.example.shortudy.domain.user.controller;

import com.example.shortudy.domain.user.dto.request.UserLoginRequest;
import com.example.shortudy.domain.user.dto.request.UserSignUpRequest;
import com.example.shortudy.domain.user.dto.response.UserLoginResponse;
import com.example.shortudy.domain.user.service.AuthService;
import com.example.shortudy.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService,JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping(path ="/signup" , consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> signup(@Valid @RequestBody UserSignUpRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // 1. 요청 헤더에서 Access Token 추출
        //    (HTTP Authorization: Bearer {Access Token} 형식에서 토큰 값만 파싱)
        String accessToken = jwtTokenProvider.resolveToken(request);

        // 2. 로그아웃 서비스 로직 호출 (Refresh Token 무효화)
        authService.logout(accessToken);

        // 클라이언트에게 성공적으로 처리되었음을 알림
        return ResponseEntity.ok().build();
    }

}