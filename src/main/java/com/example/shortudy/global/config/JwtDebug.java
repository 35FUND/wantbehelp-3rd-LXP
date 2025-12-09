package com.example.shortudy.global.config;

// language: java
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JwtDebug {

    // dev에서 확인할 server secret (application.yml 대신 임시로 전달)
    public static boolean debugValidate(String rawToken, String secret) {
        if (rawToken == null) return false;
        String token = rawToken.startsWith("Bearer ") ? rawToken.substring(7).trim() : rawToken;

        // header 디코드 (검증 없이 alg 확인)
        try {
            String[] parts = token.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            System.out.println("Header: " + headerJson); // dev 전용
            if (headerJson.contains("\"alg\":\"RS")) {
                System.out.println("토큰은 RSA 계열입니다. 서버가 RS 계열인지 확인하세요 (public/private key mismatch).");
                // RS256이면 public key로 검증해야 함
                return false;
            }

            // HS 계열이면 실제로 서명을 검증해본다
            Jwts.parser()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token); // 실패하면 예외 발생
            System.out.println("서명 검증 성공");
            return true;
        } catch (JwtException e) {
            System.out.println("서명 검증 실패: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("토큰 디코드 실패: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        // 예: dev용 토큰과 secret을 넣어 실행
        String token = "<여기에 토큰>";
        String secret = "<여기에 서버 secret 값>";
        debugValidate(token, secret);
    }
}
