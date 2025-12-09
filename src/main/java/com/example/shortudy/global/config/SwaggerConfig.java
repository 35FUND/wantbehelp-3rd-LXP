package com.example.shortudy.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger(OpenAPI) 설정
 * 접속 URL: http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT 인증 스키마
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("BearerAuth");

        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }

    private Info apiInfo() {
        return new Info()
                .title("📚 Shortudy API")
                .description("""
                        ## 숏폼 학습 플랫폼 API 문서
                        
                        ### 인증 방법
                        1. `/api/v1/auth/signup`으로 회원가입
                        2. `/api/v1/auth/login`으로 로그인하여 토큰 발급
                        3. 우측 상단 `Authorize` 버튼 클릭
                        4. `Bearer {accessToken}` 형식으로 입력
                        
                        ### 주요 기능
                        - 🎬 **Shorts**: 숏폼 영상 CRUD
                        - 📁 **Categories**: 카테고리 관리
                        - 🏷️ **Tags**: 태그 관리
                        - 👤 **Users**: 사용자 정보
                        - 🔐 **Auth**: 인증/인가
                        - 📤 **Files**: 파일 업로드
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("Shortudy Team")
                        .email("support@shortudy.com"));
    }
}

