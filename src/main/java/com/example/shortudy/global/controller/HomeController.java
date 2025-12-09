package com.example.shortudy.global.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class HomeController {

    /**
     * 루트 경로 - 메인 페이지로 리다이렉트
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    /**
     * API 상태 확인 엔드포인트
     */
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Shortudy API Server is running!",
                "endpoints", Map.of(
                        "categories", "/api/v1/categories",
                        "shorts", "/api/v1/shorts",
                        "tags", "/api/v1/tags",
                        "auth", "/api/v1/auth",
                        "users", "/api/v1/users"
                )
        ));
    }
}


