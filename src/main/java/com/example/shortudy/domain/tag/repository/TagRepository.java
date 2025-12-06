package com.example.shortudy.domain.tag.repository;

import com.example.shortudy.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNormalizedName(String normalizedName);
}
