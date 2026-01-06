package com.example.shortudy.domain.comment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRequest (

        @NotBlank @Size(min = 1, max = 1000) String content
        ){
}
