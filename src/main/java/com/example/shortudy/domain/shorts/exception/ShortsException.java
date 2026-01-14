package com.example.shortudy.domain.shorts.exception;

import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;

public class ShortsException extends BaseException {

    public ShortsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ShortsException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public static ShortsException notFound() {
        return new ShortsException(ErrorCode.SHORTS_NOT_FOUND);
    }

    public static ShortsException forbidden() {
        return new ShortsException(ErrorCode.SHORTS_FORBIDDEN);
    }

    public static ShortsException invalidDuration(String message) {
        return new ShortsException(ErrorCode.INVALID_INPUT, message);
    }
}