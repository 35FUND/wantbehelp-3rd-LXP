package com.example.shortudy.domain.tag.service;

import com.example.shortudy.domain.tag.dto.request.TagRequest;
import com.example.shortudy.domain.tag.dto.response.TagResponse;
import com.example.shortudy.domain.tag.entity.Tag;
import com.example.shortudy.domain.tag.repository.TagRepository;
import com.example.shortudy.global.util.TagNormalizer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public Tag getOrCreateTag(String rawName) {
        String normalized = TagNormalizer.normalize(rawName);
        if (normalized == null) {
            return null;
        }

        return tagRepository.findByNormalizedName(normalized)
                .orElseGet(() -> {
                    Tag tag = new Tag(rawName.trim(), normalized);
                    return tagRepository.save(tag);
                });
    }
}
