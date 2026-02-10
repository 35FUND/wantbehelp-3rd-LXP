package com.example.shortudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShortsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortsApplication.class, args);
    }
}

