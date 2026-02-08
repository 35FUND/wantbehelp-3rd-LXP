package com.example.shortudy.domain.playlist.playlist.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 플레이리스트 내 숏츠 순서 변경 요청 DTO
 * [용도]
 * - 플레이리스트 내 특정 숏츠의 위치를 변경할 때 사용
 * [JSON 요청 예시 - 숏츠 id 1을 2번 위치로 이동]
 * {
 *   "shortsId": 1,
 *   "newIndex": 2
 * }
 * [순서 변경 예시]
 * 현재 상태: [A:0, B:1, C:2, D:3]
 * 요청: shortsId=B의 숏츠 ID, newIndex=3
 * 결과: [A:0, C:1, D:2, B:3]
 */
public record PlaylistShortsReorderRequest(

        @NotNull(message = "shortsId는 필수입니다.")
        Long shortsId,

        @NotNull(message = "새 순서는 필수입니다.")
        @Min(value = 0, message = "순서는 0 이상이어야 합니다.")
        Integer newIndex
) {
}
