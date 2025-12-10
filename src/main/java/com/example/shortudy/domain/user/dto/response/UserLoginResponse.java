package com.example.shortudy.domain.user.dto.response;

import com.example.shortudy.domain.user.dto.UserResponse;

public class UserLoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserResponse user;

    public UserLoginResponse() {
    }

    public UserLoginResponse(String accessToken, String refreshToken, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserResponse getUser() {return user;}
}
