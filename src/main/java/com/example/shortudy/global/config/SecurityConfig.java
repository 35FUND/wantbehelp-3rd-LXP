package com.example.shortudy.global.config;

import com.example.shortudy.global.jwt.JwtAuthenticationFilter;
import com.example.shortudy.global.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * Spring Security 설정
 * 권한별 접근 제어 정의
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // 공개 API (인증 불필요)
                        .requestMatchers(
                                "/",
                                "/*.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/shorts",
                                "/api/v1/shorts/{id}",
                                "/api/v1/categories",
                                "/api/v1/categories/{id}",
                                "/api/v1/tags",
                                "/api/v1/tags/{id}"
                        ).permitAll()

                        // 사용자 인증 필요
                        .requestMatchers(
                                "/api/v1/auth/logout",
                                "/api/v1/auth/stat",
                                "/api/v1/users/me",
                                "/api/v1/users/me/shorts",
                                "/api/v1/shorts",
                                "/api/v1/shorts/**",
                                "/api/v1/files/**"
                        ).authenticated()

                        // 관리자 전용 (ROLE_ADMIN 필요)
                        .requestMatchers(
                                "POST",
                                "/api/v1/categories",
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "PUT",
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "DELETE",
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")

                        // 나머지 모든 요청 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}