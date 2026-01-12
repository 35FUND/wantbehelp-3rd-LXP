package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.shorts.entity.Shorts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortsRepository extends JpaRepository<Shorts, Long> {

    Optional<Shorts> findWithDetailsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findByUserId(Long userId, Pageable pageable);

    @Query(value = "SELECT s.* FROM shorts_form s ORDER BY RAND()",
           countQuery = "SELECT COUNT(*) FROM shorts_form",
           nativeQuery = true)
    Page<Shorts> findAllRandom(Pageable pageable);

    @Query("SELECT DISTINCT s FROM Shorts s LEFT JOIN FETCH s.shortsKeywords sk LEFT JOIN FETCH sk.keyword")
    List<Shorts> findAllWithKeywords();

    @EntityGraph(attributePaths = {"user", "category"})
    List<Shorts> findByIdNot(Long id);
}
