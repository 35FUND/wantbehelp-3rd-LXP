package com.example.shortudy.global.security.handler;

import com.example.shortudy.global.common.ApiResponse;
import com.example.shortudy.global.error.ErrorCode;
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

        ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");

        if (errorCode == null) {
            errorCode = ErrorCode.LOGIN_REQUIRED;
        }

        // 응답 타입을 JSON으로 설정
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.status().value());

        ApiResponse<Void> apiResponse = ApiResponse.error(
                errorCode.message(),
                errorCode.code(),
                request.getRequestURI()
        );


        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
