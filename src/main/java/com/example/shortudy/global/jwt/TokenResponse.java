package com.example.shortudy.global.jwt;

/**
 * JWT 토큰 응답 DTO
 */
public class TokenResponse {

    private String grantType;
    private String accessToken;
    private String refreshToken;


    public TokenResponse(String grantType, String accessToken, String refreshToken) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getGrantType() {
        return grantType;
    }
}
