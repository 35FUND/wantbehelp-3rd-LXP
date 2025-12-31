package com.example.shortudy.global.error;

import java.time.LocalDateTime;

public record ErrorResponse (
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String massage,
        String path
){
}
