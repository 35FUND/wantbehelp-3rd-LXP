package com.example.shortudy.domain.keyword.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    protected Keyword() {}

    private Keyword(String name) {
        this.name = name;
    }

    public static Keyword of(String name) {
        return new Keyword(name);
    }
}