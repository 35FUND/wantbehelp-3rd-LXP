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

//         HTTP 상태 코드를 401(Unauthorized)로 설정
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");

        if (errorCode == null) {
//            errorCode = errorCode.valueOf(request.getParameter("error"));
        }
//        Http 상태 코드 설정
//        response.setStatus(errorCode.getHHttpStatus.value());

        // 응답 타입을 JSON으로 설정
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.error(
//                errorCode.getMessage(),
//                errorCode.getCode
                //TODO 수정 해야 함..
                null,
                null,
                null
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
