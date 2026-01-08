package com.example.shortudy.domain.keyword.service;

import com.example.shortudy.domain.keyword.dto.request.TagRequest;
import com.example.shortudy.domain.keyword.dto.response.TagResponse;
import com.example.shortudy.domain.keyword.entity.Tag;
import com.example.shortudy.domain.keyword.repository.TagRepository;
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

    // TODO Tagging 엔티티 삭제되면서 주석 처리됨 (추후 수정 바랍니다)

//    @Transactional
//    public Tag getOrCreateTag(String rawName) {
//        String normalized = TagNormalizer.normalize(rawName);
//        if (normalized == null) {
//            return null;
//        }
//
//        return tagRepository.findByNormalizedName(normalized)
//                .orElseGet(() -> {
//                    Tag tag = new Tag(rawName.trim(), normalized);
//                    return tagRepository.save(tag);
//                });
//    }

//    @Transactional
//    public TagResponse createTag(TagRequest request) {
//        String normalized = TagNormalizer.normalize(request.name());
//
//        Tag tag = new Tag(request.name().trim();
//        Tag savedTag = tagRepository.save(tag);
//        return new TagResponse(savedTag.getId(), savedTag.getDisplayName());
//    }

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

        return new TagResponse(tag.getId(), tag.getDisplayName());
    }

    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다: " + tagId));
        tagRepository.delete(tag);
    }
}
