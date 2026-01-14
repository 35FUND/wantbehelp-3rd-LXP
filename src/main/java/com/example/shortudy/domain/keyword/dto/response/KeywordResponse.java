package com.example.shortudy.domain.keyword.dto.response;

import jakarta.validation.constraints.NotBlank;

public record KeywordResponse(Long id,
        @NotBlank String displayName,String normalizedName){}