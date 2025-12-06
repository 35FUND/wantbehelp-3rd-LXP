package com.example.shortudy.domain.shorts.entity;


import jakarta.persistence.*;

@Entity
public class ShortFormTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorts_id")
    private Shorts shorts;

    public ShortFormTag() {
    }

    public Long getId() {
        return id;
    }

    public Shorts getShorts() {
        return shorts;
    }
}
