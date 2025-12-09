package com.example.shortudy.global.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * 파일 저장 서비스
 * - 비디오/썸네일 파일 업로드 처리
 * - 파일 확장자 검증으로 보안 강화
 */
@Service
public class FileStorageService {

    // 허용된 비디오 확장자
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of(
            ".mp4", ".webm", ".mov", ".avi", ".mkv"
    );

    // 허용된 이미지 확장자
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private final Path uploadPath;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
            Files.createDirectories(this.uploadPath.resolve("videos"));
            Files.createDirectories(this.uploadPath.resolve("thumbnails"));
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    /**
     * 비디오 파일 저장
     * @return 저장된 파일의 URL 경로
     */
    public String storeVideo(MultipartFile file) {
        return storeFile(file, "videos", ALLOWED_VIDEO_EXTENSIONS);
    }

    /**
     * 썸네일 파일 저장
     * @return 저장된 파일의 URL 경로
     */
    public String storeThumbnail(MultipartFile file) {
        return storeFile(file, "thumbnails", ALLOWED_IMAGE_EXTENSIONS);
    }

    private String storeFile(MultipartFile file, String subDir, Set<String> allowedExtensions) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);

        // 확장자 검증
        validateExtension(extension, allowedExtensions, subDir);

        // UUID로 파일명 생성 (중복 방지)
        String newFilename = UUID.randomUUID().toString() + extension;

        try {
            Path targetPath = this.uploadPath.resolve(subDir).resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 접근 가능한 URL 경로 반환
            return "/uploads/" + subDir + "/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다: " + originalFilename, e);
        }
    }

    /**
     * 파일명에서 확장자 추출
     */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 확장자 유효성 검증
     */
    private void validateExtension(String extension, Set<String> allowedExtensions, String type) {
        if (extension.isEmpty() || !allowedExtensions.contains(extension)) {
            String typeKorean = "videos".equals(type) ? "비디오" : "이미지";
            throw new IllegalArgumentException(
                    "지원하지 않는 " + typeKorean + " 파일 형식입니다. " +
                    "허용된 확장자: " + String.join(", ", allowedExtensions)
            );
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }

        try {
            String relativePath = fileUrl.substring("/uploads/".length());
            Path filePath = this.uploadPath.resolve(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 삭제 실패해도 무시 (로깅만)
        }
    }
}

