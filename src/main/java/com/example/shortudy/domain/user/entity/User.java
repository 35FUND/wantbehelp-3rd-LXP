package com.example.shortudy.domain.user.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
//쉐도우 복싱용 유저
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;


    public User(String nickname) {
        this.nickname = nickname;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }
}

