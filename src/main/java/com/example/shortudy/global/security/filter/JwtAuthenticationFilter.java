package com.example.shortudy.global.security.filter;

import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import com.example.shortudy.global.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

//    private static final String AUTHORIZATION_HEADER = "Authorization";
//    private static final String BEARER_PREFIX = "Bearer ";
//    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 컨트롤러에 요청이 도착하기 전에 필터가 요청을 가로챈다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 헤더에서 accessToken을 꺼낸다.
            String token = resolveToken(request);

            // validateToken으로 검증
            if (token != null) {
                jwtTokenProvider.validateToken(token);
                // 인증된 토큰에 주어지는 합격 목걸이, 합격한 유저 정보를 SecurityContext 바구니에 저장
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (BaseException e) { // BaseException 이 발생하면 EntryPoint가 사용할 수 있게 배달
            request.setAttribute("exception", e.errorCode());
        } catch (Exception e) {     // 예상치 못한 에러가 발생했을 때
            request.setAttribute("exception", ErrorCode.INTERNAL_ERROR);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    //    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String token = resolveToken(request);
//
//        // 토큰이 유효하면 인증 정보 설정
//        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
//            String email = jwtTokenProvider.getEmailFromToken(token);
//            List<String> roles = jwtTokenProvider.getRolesFromToken(token);
//
//            List<SimpleGrantedAuthority> authorities = roles.stream()
//                    .map(SimpleGrantedAuthority::new)
//                    .toList();
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(email, null, authorities);
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        filterChain.doFilter(request, response);
//    }

    /**
     * Authorization 헤더 또는 쿠키에서 토큰 추출
     * 1. Authorization 헤더 확인 (Bearer 토큰)
     * 2. 쿠키 확인 (accessToken)
     */
//    private String resolveToken(HttpServletRequest request) {
//        // 1. Authorization 헤더에서 토큰 추출
//        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
//            return bearerToken.substring(BEARER_PREFIX.length());
//        }
//
//        // 2. 쿠키에서 Access Token 추출
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
//                    return cookie.getValue();
//                }
//            }
//        }
//
//        return null;
//    }
}

