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
import java.util.stream.Collectors;

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

    @Transactional
    public TagResponse createTag(TagRequest request) {
        String normalized = TagNormalizer.normalize(request.name());

        // 이미 존재하는 태그인지 확인
        if (tagRepository.findByNormalizedName(normalized).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 태그입니다: " + request.name());
        }

        Tag tag = new Tag(request.name().trim(), normalized);
        Tag savedTag = tagRepository.save(tag);
        return new TagResponse(savedTag.getId(), savedTag.getDisplayName());
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getDisplayName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TagResponse getTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다: " + tagId));
        return new TagResponse(tag.getId(), tag.getDisplayName());
    }

    @Transactional
    public TagResponse updateTag(Long tagId, TagRequest request) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다: " + tagId));

        String normalized = TagNormalizer.normalize(request.name());

        // 다른 태그와 중복되는지 확인 (자기 자신 제외)
        tagRepository.findByNormalizedName(normalized)
                .filter(existingTag -> !existingTag.getId().equals(tagId))
                .ifPresent(existingTag -> {
                    throw new IllegalArgumentException("이미 존재하는 태그입니다: " + request.name());
                });

        // ID 유지하면서 이름만 업데이트
        tag.updateName(request.name().trim(), normalized);

        return new TagResponse(tag.getId(), tag.getDisplayName());
    }

    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다: " + tagId));
        tagRepository.delete(tag);
    }
}
