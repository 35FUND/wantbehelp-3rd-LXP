package com.example.shortudy.domain.user.service;

import com.example.shortudy.domain.user.dto.request.TokenRefreshRequest;
import com.example.shortudy.domain.user.dto.request.UserLoginRequest;
import com.example.shortudy.domain.user.dto.request.UserSignUpRequest;
import com.example.shortudy.domain.user.dto.response.AuthStatusResponse;
import com.example.shortudy.domain.user.dto.response.UserLoginResponse;

import java.util.Map;

public interface AuthService {
    void signup(UserSignUpRequest request);
    Map<String, Object> login(UserLoginRequest request);  // 토큰 + UserLoginResponse 반환
    void logout(String email);
    AuthStatusResponse getAuthStatus(String email);
    Map<String, Object> refreshToken(TokenRefreshRequest request);  // 토큰 + UserLoginResponse 반환
}
