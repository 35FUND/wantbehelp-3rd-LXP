package com.example.shortudy.domain.shorts.upload.repository;

import com.example.shortudy.domain.shorts.upload.entity.ShortsUploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShortsUploadSessionRepository extends JpaRepository<ShortsUploadSession, Long> {
    Optional<ShortsUploadSession> findByShortId(Long shortId);
}
