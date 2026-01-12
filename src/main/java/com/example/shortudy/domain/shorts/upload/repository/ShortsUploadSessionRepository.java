package com.example.shortudy.domain.shorts.upload.repository;

import com.example.shortudy.domain.shorts.upload.entity.ShortsUploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortsUploadSessionRepository extends JpaRepository<ShortsUploadSession, Long> {
}
