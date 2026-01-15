package com.example.shortudy.domain.shorts.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class ShortsPopularRequest {

    private final Long cursorScore;
    private final Long cursorId;

    @Min(1) @Max(50)
    private final int size;

    public ShortsPopularRequest(Long cursorScore, Long cursorId, Integer size) {
        this.cursorScore = cursorScore;
        this.cursorId = cursorId;
        this.size = (size == null) ? 10 : size;
    }
}
