package com.example.shortudy.domain.user.dto;

public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String profileImageUrl;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, String name, String nickname, String profileImageUrl) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {return nickname;}

    public void setNickname(String nickname) {this.nickname = nickname;}

    public String getProfileImageUrl() {return profileImageUrl;}

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}

