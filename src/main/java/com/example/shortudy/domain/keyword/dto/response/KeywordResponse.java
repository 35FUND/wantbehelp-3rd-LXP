package com.example.shortudy.domain.keyword.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record KeywordResponse(
        @NotBlank @JsonProperty("name") String displayName){}



/*
* JSON에선 name 필드로 노출/매핑하려고 사용
*  기본 값 displayName -> name으로 보여주는 값 변경
* */