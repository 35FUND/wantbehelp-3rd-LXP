package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.shorts.entity.ShortsInspectionResults;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortsInspectionResultsRepository extends JpaRepository<ShortsInspectionResults, Long> {

    Optional<ShortsInspectionResults> findByShortsId(Long shortsId);
}
