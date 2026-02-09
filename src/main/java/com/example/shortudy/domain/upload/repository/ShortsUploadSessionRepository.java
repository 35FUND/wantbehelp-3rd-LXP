package com.example.shortudy.domain.upload.repository;

import com.example.shortudy.domain.upload.entity.ShortsUploadSession.UploadStatus;
import com.example.shortudy.domain.upload.entity.ShortsUploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

public interface ShortsUploadSessionRepository extends JpaRepository<ShortsUploadSession, Long> {
    Optional<ShortsUploadSession> findByShortId(Long shortId);

    List<ShortsUploadSession> findByUserIdAndStatus(Long userId, UploadStatus status);

    List<ShortsUploadSession> findByStatusAndCreatedAtBefore(UploadStatus status, LocalDateTime cutoff);
}
