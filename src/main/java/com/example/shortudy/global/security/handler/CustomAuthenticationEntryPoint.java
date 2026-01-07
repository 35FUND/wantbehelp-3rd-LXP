package com.example.shortudy.global.security.handler;

import com.example.shortudy.global.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        // HTTP 상태 코드를 401(Unauthorized)로 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 응답 타입을 JSON으로 설정
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.error("인증 정보가 유효하지 않습니다.");

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
