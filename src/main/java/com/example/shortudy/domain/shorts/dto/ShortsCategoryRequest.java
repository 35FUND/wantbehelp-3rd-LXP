package com.example.shortudy.domain.shorts.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class ShortsCategoryRequest {

    private final Long cursorId;

    @Min(1) @Max(50)
    private final int size;

    public ShortsCategoryRequest(Long cursorId, Integer size) {
        this.cursorId = cursorId;
        this.size = (size == null) ? 10 : size;
    }
}
