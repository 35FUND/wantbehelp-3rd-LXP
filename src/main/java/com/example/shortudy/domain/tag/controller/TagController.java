package com.example.shortudy.domain.tag.controller;

import com.example.shortudy.domain.tag.dto.request.TagRequest;
import com.example.shortudy.domain.tag.dto.response.TagResponse;
import com.example.shortudy.domain.tag.entity.Tag;
import com.example.shortudy.domain.tag.repository.TagRepository;
import com.example.shortudy.domain.tag.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }


}
