package com.example.shortudy.domain.playlist.dto.request;

import com.example.shortudy.domain.playlist.entity.PlaylistVisibility;
import jakarta.validation.constraints.Size;

public record PlaylistUpdateRequest(

        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
    String description,

    @Size(max = 500, message = "썸네일 URL은 500자를 초과할 수 없습니다.")
    String thumbnailUrl,

        PlaylistVisibility visibility
) {
}
