package com.example.shortudy.domain.shorts.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class ShortsCursorPageResponse {
    private final List<ShortsResponse> shorts;
    private final boolean hasNext;
    private final Long nextCursorId;
    private final Long nextCursorScore;

    public ShortsCursorPageResponse(List<ShortsResponse> shorts, boolean hasNext, Long nextCursorId, Long nextCursorScore) {
        this.shorts = shorts;
        this.hasNext = hasNext;
        this.nextCursorId = nextCursorId;
        this.nextCursorScore = nextCursorScore;
    }

    public static ShortsCursorPageResponse of(List<ShortsResponse> shorts, boolean hasNext, Long nextCursorId, Long nextCursorScore) {
        return new ShortsCursorPageResponse(shorts, hasNext, nextCursorId, nextCursorScore);
    }
}