package com.example.shortudy.domain.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String tokenValue;

    private LocalDateTime expiryDate;

    protected RefreshToken() {}

    public RefreshToken(String userEmail, String tokenValue, LocalDateTime expiryDate) {
        this.userEmail = userEmail;
        this.tokenValue = tokenValue;
        this.expiryDate = expiryDate;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}
