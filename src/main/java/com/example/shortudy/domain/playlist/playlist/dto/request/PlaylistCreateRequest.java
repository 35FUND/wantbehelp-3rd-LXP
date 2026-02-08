package com.example.shortudy.domain.playlist.playlist.dto.request;

import com.example.shortudy.domain.playlist.entity.PlaylistVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlaylistCreateRequest(

        @NotBlank(message = "플레이리스트 제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
        String description,

        /**
         * 생성 시 함께 추가할 숏츠 ID 목록 (선택)
         * - null이거나 빈 리스트이면 빈 플레이리스트 생성
         */
        List<Long> shortsIds,

        /**
         * 플레이리스트 썸네일로 사용할 숏츠 ID (선택)
         * - shortsIds에 포함된 숏츠만 지정 가능
         * - null이면 자동 썸네일 (첫 번째 숏츠 썸네일 사용)
         */
        Long thumbnailShortsId,

        PlaylistVisibility visibility
) {
}
