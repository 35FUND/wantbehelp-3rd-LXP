package com.example.shortudy.domain.shorts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Getter
@Table(name = "shorts_inspection_results")
public class ShortsInspectionResults implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "shorts_id")
    private Shorts shorts;

    @Column(name = "author")
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_status")
    private InspectionStatus inspectionStatus;

    @Column(name = "category")
    private String category;

    @Column(name = "confidence_score")
    private Float confidenceScore;

    @Column(name = "reason")
    private String reason;

    @Column(name = "registed_at")
    private LocalDateTime registedAt;


}
