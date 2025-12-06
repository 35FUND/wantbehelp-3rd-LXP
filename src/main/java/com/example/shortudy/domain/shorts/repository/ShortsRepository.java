package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.shorts.entity.Shorts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortsRepository extends JpaRepository<Shorts, Long> {
}

