package com.example.shortudy.global.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/**")
              .allowedOrigins("http://localhost:3000")
              .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
              .allowedHeaders("*")
              .allowCredentials(true)
              .maxAge(3600);
    }
}
