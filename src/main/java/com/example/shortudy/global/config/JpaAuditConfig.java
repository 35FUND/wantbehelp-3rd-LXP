package com.example.shortudy.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 엔티티 데이터의 생성/수정 시간을 자동으로 관리해주는 설정 클래스
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}
