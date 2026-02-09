package com.example.shortudy.domain.like.dto;

import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;

import java.util.Arrays;

/**
 * 좋아요 정렬 기준 enum
 * - latest: 최신 순
 * - popular: 인기 순(like count)
 */
public enum SortStandard {
    LATEST("latest"),
    POPULAR("popular");

    private final String value;

    SortStandard(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 문자열로 해당하는 enum값 찾기
     * @param value 문자열
     * @return 해당하는 enum값
     */
    public static SortStandard fromValue(String value) {
        return Arrays.stream(SortStandard.values())
                .filter(v -> v.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT));
    }
}
