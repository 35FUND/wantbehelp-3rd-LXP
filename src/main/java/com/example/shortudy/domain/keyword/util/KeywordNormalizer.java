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

    public static String normalizeForSearch(String raw) {
        if (raw == null) throw new BaseException(ErrorCode.INVALID_INPUT);
        String s = raw.trim();
        if (s.isBlank()) throw new BaseException(ErrorCode.INVALID_INPUT);

        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = NON_PRINTABLE.matcher(s).replaceAll("");


        s = s.toLowerCase();

        if (ANY_WHITESPACE.matcher(s).find()) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }

        s = s.toLowerCase();

        if (s.isBlank()) throw new BaseException(ErrorCode.INVALID_INPUT);
        return s;
    }

    public static String normalize(String raw) {
        return normalizeForSearch(raw);
    }
}
