package com.example.shortudy.domain.keyword.dto.response;

import com.example.shortudy.domain.keyword.entity.Tag;

public record TagResponse (
        Long id,

        String name
){
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getDisplayName());
    }
}
