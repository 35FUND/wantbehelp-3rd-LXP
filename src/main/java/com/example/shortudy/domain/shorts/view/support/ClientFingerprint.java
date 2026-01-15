package com.example.shortudy.domain.shorts.view.support;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClientFingerprint {

    private final String ip;
    private final String userAgent;

    private ClientFingerprint(String ip, String userAgent) {
        this.ip = ip;
        this.userAgent = userAgent;
    }

    // 요청 정보를 기반으로 클라이언트 지문 생성
    public static ClientFingerprint from(HttpServletRequest request) {
        String ip = resolveClientIp(request);
        String userAgent = request == null ? "" : String.valueOf(request.getHeader("User-Agent"));
        return new ClientFingerprint(ip, userAgent);
    }

    // 지문을 조회수 집계용 식별자로 변환
    public String toIdentifier() {
        String raw = ip + "|" + userAgent;
        return sha256(raw);
    }

    // 클라이언트 IP 추출
    private static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    // SHA-256 해시 생성
    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
