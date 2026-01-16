package com.example.shortudy.domain.keyword.repository;

import com.example.shortudy.domain.keyword.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    @Query("select k from Keyword k where " +
            "lower(k.displayName) like lower(concat('%', :keyword1, '%')) " +
            "or lower(k.normalizedName) like lower(concat('%', :keyword2, '%'))")
    List<Keyword> searchKeyword(@Param("keyword1") String keyword1,
                                @Param("keyword2") String keyword2);
}