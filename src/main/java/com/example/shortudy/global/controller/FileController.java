package com.example.shortudy.global.controller;

import com.example.shortudy.global.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 비디오 파일 업로드
     * POST /api/v1/files/videos
     */
    @PostMapping("/videos")
    public ResponseEntity<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        String videoUrl = fileStorageService.storeVideo(file);
        return ResponseEntity.ok(Map.of(
                "url", videoUrl,
                "message", "비디오 업로드 성공"
        ));
    }

    /**
     * 썸네일 파일 업로드
     * POST /api/v1/files/thumbnails
     */
    @PostMapping("/thumbnails")
    public ResponseEntity<Map<String, String>> uploadThumbnail(@RequestParam("file") MultipartFile file) {
        String thumbnailUrl = fileStorageService.storeThumbnail(file);
        return ResponseEntity.ok(Map.of(
                "url", thumbnailUrl,
                "message", "썸네일 업로드 성공"
        ));
    }
}

