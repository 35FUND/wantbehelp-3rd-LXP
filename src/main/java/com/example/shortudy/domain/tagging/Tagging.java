package com.example.shortudy.domain.tagging;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.tag.entity.Tag;
import com.example.shortudy.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import static jakarta.persistence.GenerationType.*;

@Entity
@Table(
        name = "tagging",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tagging_shorts_id_tag_id",
                        columnNames = {"shorts_id", "tag_id"}
                )
        }
)
public class Tagging extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // 어떤 숏폼에 달린 태그인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorts_id", nullable = false)
    private Shorts shorts;

    // 어떤 태그인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    protected Tagging() {
    }

    public Tagging(Shorts shorts, Tag tag) {
        this.shorts = shorts;
        this.tag = tag;
    }

    public Long getId() {
        return id;
    }

    public Shorts getShorts() {
        return shorts;
    }

    public Tag getTag() {
        return tag;
    }
}
