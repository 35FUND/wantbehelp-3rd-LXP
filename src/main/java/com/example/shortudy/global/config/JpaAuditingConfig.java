package com.example.shortudy.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화 설정
 * BaseEntity의 @CreatedDate, @LastModifiedDate가 동작하도록 함
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
