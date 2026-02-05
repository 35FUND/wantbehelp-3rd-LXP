package com.example.shortudy.domain.playlist.dto.request;

import jakarta.validation.constraints.NotNull;


public record PlaylistShortsAddRequest(
        @NotNull(message = "Shorts ID는 필수입니다.")
        Long shortsId
) {
}
