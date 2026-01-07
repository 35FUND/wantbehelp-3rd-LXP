package com.example.shortudy.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "refreshtokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String token;

    protected RefreshToken() {}

    private RefreshToken(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public static RefreshToken create(String email, String token) {
        return new RefreshToken(email, token);
    }

    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
