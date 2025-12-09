package com.example.shortudy.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // static 폴더 (테스트용 샘플 영상)
        // 접근: http://localhost:8080/static/sample.mp4
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // uploads 폴더 (실제 업로드된 파일)
        // 접근: http://localhost:8080/uploads/videos/xxx.mp4
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/");
    }
}

