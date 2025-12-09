package com.example.shortudy.domain.user.dto;

import com.example.shortudy.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 응답 DTO (내 정보 조회용)
 * - email: 본인만 볼 수 있음
 * - password: 절대 노출 X
 */
@Schema(description = "사용자 응답")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "이메일 (본인만 조회 가능)", example = "user@example.com")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "닉네임", example = "길동이")
        String nickname
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname()
        );
    }

    /**
     * 공개 프로필용 (다른 사용자가 볼 때)
     * - email 제외
     */
    public static UserResponse publicProfile(User user) {
        return new UserResponse(
                user.getId(),
                null,  // email 숨김
                user.getName(),
                user.getNickname()
        );
    }
}

