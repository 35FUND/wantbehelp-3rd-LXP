package com.example.shortudy.domain.user.service;

import com.example.shortudy.domain.user.dto.request.UserLoginRequest;
import com.example.shortudy.domain.user.dto.request.UserSignUpRequest;
import com.example.shortudy.domain.user.dto.response.UserLoginResponse;

public interface AuthService {
    void signup(UserSignUpRequest request);
    UserLoginResponse login(UserLoginRequest request);

    void logout(String accessToken);
}
