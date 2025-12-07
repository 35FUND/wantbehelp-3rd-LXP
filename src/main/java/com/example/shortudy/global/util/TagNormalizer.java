package com.example.shortudy.global.util;

public class TagNormalizer {

        public static String normalize(String raw) {

            String result = raw.trim();
            if (result.isEmpty()) return null;

            result = result.replaceAll("\\s+", " ");

            result = result.toLowerCase();

            return result;
        }
}
