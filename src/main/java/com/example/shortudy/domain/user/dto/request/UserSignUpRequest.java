package com.example.shortudy.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 요청")
public class UserSignUpRequest {

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "비밀번호 (8자 이상)", example = "password123")
    private String password;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    public UserSignUpRequest() {
    }

    public UserSignUpRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
