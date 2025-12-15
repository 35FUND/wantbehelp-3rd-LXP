package com.example.shortudy.domain.tag.entity;

import com.example.shortudy.domain.tagging.Tagging;
import com.example.shortudy.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tag")
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자에게 보여줄 값 ex) JAVA
    @Column(nullable = false, length = 50)
    private String displayName;

    // 정규화 로직(공백 제거, 소문자로 변환 등) 거치고 실제 DB에 저장될 값 ex) java
    @Column(nullable = false, length = 50, unique = true)
    private String normalizedName;

    @OneToMany(mappedBy = "tag")
    private List<Tagging> taggings = new ArrayList<>();

    protected Tag() {}

    public Tag(String displayName, String normalizedName) {
        this.displayName = displayName;
        this.normalizedName = normalizedName;
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public List<Tagging> getTaggings() {
        return taggings;
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
