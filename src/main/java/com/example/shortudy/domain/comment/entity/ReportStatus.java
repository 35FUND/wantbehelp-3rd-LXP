package com.example.shortudy.domain.comment.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 신고 처리 상태 열거형
 */
@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    PENDING("대기중"),
    PROCESSED("처리완료"),
    REJECTED("반려");

    private final String description;
}
