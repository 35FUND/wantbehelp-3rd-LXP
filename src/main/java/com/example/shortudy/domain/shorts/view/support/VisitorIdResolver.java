package com.example.shortudy.domain.shorts.view.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class VisitorIdResolver {

    // 사용자 또는 익명 방문자를 위한 식별자 생성
    public String resolve(Long userId, HttpServletRequest request) {
        if (userId != null) {
            return "user:" + userId;
        }
        return "guest:" + ClientFingerprint.from(request).toIdentifier();
    }
}
