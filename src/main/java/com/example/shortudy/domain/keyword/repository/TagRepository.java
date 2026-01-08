package com.example.shortudy.domain.keyword.repository;

import com.example.shortudy.domain.keyword.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

}
