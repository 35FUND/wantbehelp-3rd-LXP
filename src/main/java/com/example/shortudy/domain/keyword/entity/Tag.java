package com.example.shortudy.domain.keyword.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자에게 보여줄 값 ex) JAVA
    @Column(nullable = false, length = 50)
    private String displayName;

    // 정규화 로직(공백 제거, 소문자로 변환 등) 거치고 실제 DB에 저장될 값 ex) java
    @Column(nullable = false, length = 50, unique = true)
    private String normalizedName;

    protected Tag() {}

    public Tag(String displayName, String normalizedName) {
        this.displayName = displayName;
        this.normalizedName = normalizedName;
    }

    /**
     * 태그 이름 업데이트
     * - ID를 유지하면서 이름만 변경
     */
    public void updateName(String displayName, String normalizedName) {
        this.displayName = displayName;
        this.normalizedName = normalizedName;
    }
}
