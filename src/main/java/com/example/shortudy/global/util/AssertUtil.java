package com.example.shortudy.global.util;

import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;

/**
 * 공통 Validation 기능
 */
public class AssertUtil {

    /**
     * NOT NULL 체크
     * @param object 체크 할 객체
     * @param message 에러 메시지
     */
    public static void notNull(Object object, String message) {
        if(object == null) {
            throw new BaseException(ErrorCode.INVALID_INPUT, message);
        }
    }

}
