package com.example.shortudy.domain.upload.repository;

import com.example.shortudy.domain.upload.entity.ShortsUploadSession.UploadStatus;
import com.example.shortudy.domain.upload.entity.ShortsUploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

public interface ShortsUploadSessionRepository extends JpaRepository<ShortsUploadSession, Long> {
    Optional<ShortsUploadSession> findByShortId(Long shortId);

    // 재업로드 시 기존 미완료(INITIATED) 세션을 즉시 정리하기 위한 조회
    List<ShortsUploadSession> findByUserIdAndStatus(Long userId, UploadStatus status);

    // 배치 정리용: 특정 시점 이전의 미완료(INITIATED) 세션 조회
    List<ShortsUploadSession> findByStatusAndCreatedAtBefore(UploadStatus status, LocalDateTime createdAt);
}
