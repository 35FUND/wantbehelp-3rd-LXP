package com.example.shortudy.domain.keyword.util;

import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class KeywordNormalizer {

    private static final Pattern ANY_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern NON_PRINTABLE = Pattern.compile("\\p{C}");

    private KeywordNormalizer() {
    }

    /*
     * 입력 검증 및 정규화:
     *  - null 또는 빈 문자열이면 예외 발생(IllegalArgumentException)
     *  - 유니코드 정규화(NFC), 제어문자 제거
     *  - 내부 공백 (스페이/탭/개행 등)이 있으면 IllegalArgumentException 발생
     * - 소문자화 및 앞뒤 공백 제거 후 반환
     * */

    public static String normalize(String raw) {
        if (raw == null) throw new BaseException(ErrorCode.INVALID_INPUT);
        String s = raw.trim();
        if (s.isBlank()) throw new IllegalArgumentException("값은 빈값(공백)일 수 없습니다.");

        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = NON_PRINTABLE.matcher(s).replaceAll(""); // 제어문자 제거

        // 내부 공백(스페이스 , 탭 , 개행 등)허용하지 않음
        if (ANY_WHITESPACE.matcher(s).find()) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }

        s = s.toLowerCase();

        if (s.isBlank()) throw new IllegalArgumentException("유효한 문자열이 아닙니다.");
        return s;
    }
}
