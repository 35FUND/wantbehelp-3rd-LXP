package com.example.shortudy.domain.category.entity;

import com.example.shortudy.domain.category.dto.request.CategoryRequest;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private Long parentId = null;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Category() {}

    public Category(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}

