package com.example.shortudy.domain.keyword.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "keyword")
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자에게 보여줄 값 ex) JAVA
    @Column(nullable = false, length = 50)
    private String displayName;

    // 정규화된 값 ex) java (unique)
    @Column(nullable = false, length = 50, unique = true)
    private String normalizedName;

    protected Keyword() {}

    public Keyword(String displayName, String normalizedName) {
        this.displayName = displayName;
        this.normalizedName = normalizedName;
    }
    public void updateName(String displayName, String normalizedName) {
        this.displayName = displayName;
        this.normalizedName = normalizedName;
    }
}
