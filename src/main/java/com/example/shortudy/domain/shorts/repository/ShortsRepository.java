package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.shorts.entity.Shorts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortsRepository extends JpaRepository<Shorts, Long> {

    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Shorts> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Shorts> findAll(Pageable pageable);
}

