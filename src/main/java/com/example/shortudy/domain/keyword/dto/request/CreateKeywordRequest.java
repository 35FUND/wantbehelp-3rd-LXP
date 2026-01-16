// java
package com.example.shortudy.domain.keyword.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateKeywordRequest {

    @NotBlank
    private final String displayName;

    @JsonCreator
    public CreateKeywordRequest(@JsonProperty("displayName") String displayName) {
        this.displayName = displayName;
    }
}