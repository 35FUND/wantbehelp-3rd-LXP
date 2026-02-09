package com.example.shortudy.domain.playlist.dto.response;

import com.example.shortudy.domain.user.entity.User;

/**
 * 플레이리스트 소유자 정보 DTO
 * - PlaylistResponse, PlaylistDetailResponse 등에서 공통으로 사용
 */
public record OwnerInfo(
        Long id,            // 소유자 ID
        String nickname,    // 닉네임
        String profileUrl   // 프로필 이미지 URL
) {
    public static OwnerInfo from(User user) {
        return new OwnerInfo(
                user.getId(),
                user.getNickname(),
                user.getProfileUrl()
        );
    }
}
