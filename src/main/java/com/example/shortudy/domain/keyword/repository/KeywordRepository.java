package com.example.shortudy.domain.keyword.repository;

import com.example.shortudy.domain.keyword.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    List<Keyword> findByDisplayNameContainingIgnoreCaseOrNormalizedNameContainingIgnoreCase(String keyword1, String keyword2);
}
