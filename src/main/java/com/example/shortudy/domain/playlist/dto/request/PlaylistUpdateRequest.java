package com.example.shortudy.domain.playlist.dto.request;

import com.example.shortudy.domain.playlist.entity.PlaylistVisibility;
import jakarta.validation.constraints.Size;

public record PlaylistUpdateRequest(

        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
        String description,

        /*
         * 플레이리스트 썸네일로 사용할 숏츠 ID
         * - 플레이리스트 내 숏츠의 썸네일 중 선택
         * - null이면 기존 썸네일 유지
         * - 자동 썸네일로 초기화하려면 별도 API 또는 0 등의 약속된 값 사용
         */
        Long thumbnailShortsId,

        PlaylistVisibility visibility
) {
}
