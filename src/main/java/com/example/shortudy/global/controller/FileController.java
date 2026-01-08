//package com.example.shortudy.global.controller;
//
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/files")
//public class FileController {
//
//    private final FileStorageService fileStorageService;
//
//    public FileController(FileStorageService fileStorageService) {
//        this.fileStorageService = fileStorageService;
//    }
//
//    @Operation(summary = "비디오 업로드", description = "비디오 파일을 업로드합니다. (mp4, webm, mov 지원)")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "업로드 성공"),
//            @ApiResponse(responseCode = "400", description = "지원하지 않는 파일 형식")
//    })
//    @PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Map<String, String>> uploadVideo(
//            @Parameter(description = "비디오 파일 (mp4, webm, mov)")
//            @RequestParam("file") MultipartFile file) {
//        String videoUrl = fileStorageService.storeVideo(file);
//        return ResponseEntity.ok(Map.of(
//                "url", videoUrl,
//                "message", "비디오 업로드 성공"
//        ));
//    }
//
//    @Operation(summary = "썸네일 업로드", description = "썸네일 이미지를 업로드합니다. (jpg, png, webp 지원)")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "업로드 성공"),
//            @ApiResponse(responseCode = "400", description = "지원하지 않는 파일 형식")
//    })
//    @PostMapping(value = "/thumbnails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Map<String, String>> uploadThumbnail(
//            @Parameter(description = "썸네일 이미지 (jpg, png, webp)")
//            @RequestParam("file") MultipartFile file) {
//        String thumbnailUrl = fileStorageService.storeThumbnail(file);
//        return ResponseEntity.ok(Map.of(
//                "url", thumbnailUrl,
//                "message", "썸네일 업로드 성공"
//        ));
//    }
//}
//
