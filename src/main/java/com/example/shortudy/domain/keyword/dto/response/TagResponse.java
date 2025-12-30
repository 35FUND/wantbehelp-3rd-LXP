package com.example.shortudy.domain.keyword.dto.response;

import com.example.shortudy.domain.keyword.entity.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "태그 응답")
public record TagResponse (
        @Schema(description = "태그 ID", example = "1")
        Long id,

        @Schema(description = "태그 이름", example = "Java")
        String name
){
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getDisplayName());
    }
}
