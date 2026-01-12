package com.example.shortudy.domain.category.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
@Entity
@Table(name = "category")
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Category(String name) {
        this.name = name;
        this.status = CategoryStatus.ACTIVE;
    }

    protected Category() {}

    public void updateName(String name) {

        this.name = name;
    }
    public void updateStatus(CategoryStatus status) {
        this.status = status;
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = CategoryStatus.ACTIVE;
        }
    }
    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

