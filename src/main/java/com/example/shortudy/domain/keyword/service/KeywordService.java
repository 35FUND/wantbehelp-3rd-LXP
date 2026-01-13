package com.example.shortudy.domain.keyword.service;

import com.example.shortudy.domain.keyword.dto.request.KeywordRequest;
import com.example.shortudy.domain.keyword.dto.response.KeywordResponse;
import com.example.shortudy.domain.keyword.entity.Keyword;
import com.example.shortudy.domain.keyword.repository.KeywordRepository;
import com.example.shortudy.domain.keyword.util.KeywordNormalizer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public KeywordService(KeywordRepository keywordRepository) {
        this.keywordRepository = keywordRepository;
    }

    @Transactional
    public Keyword getOrCreateKeyword(String rawName) {
        String normalized = KeywordNormalizer.normalize(rawName);

        return keywordRepository.findByNormalizedName(normalized)
                .orElseGet(() -> {
                    Keyword keyword = new Keyword(rawName.trim(), normalized);
                    return keywordRepository.save(keyword);
                });
    }

    @Transactional
    public KeywordResponse createKeyword(KeywordRequest request) {
        String normalized = KeywordNormalizer.normalize(request.name());

        String displayName = request.name().trim();
        if (keywordRepository.existsByNormalizedName(normalized)) {
            throw new IllegalArgumentException("이미 존재하는 키워드입니다.");
        }
        Keyword keyword = new Keyword(displayName, normalized);
        Keyword saved = keywordRepository.save(keyword);
        return new KeywordResponse(saved.getId(), saved.getDisplayName());
    }

    @Transactional(readOnly = true)
    public List<KeywordResponse> getAllKeywords() {
        return keywordRepository.findAll().stream()
                .map(k -> new KeywordResponse(k.getId(), k.getDisplayName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public KeywordResponse getKeyword(Long keywordId) {
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new EntityNotFoundException("키워드를 찾을 수 없습니다: " + keywordId));
        return new KeywordResponse(keyword.getId(), keyword.getDisplayName());
    }

    @Transactional
    public KeywordResponse updateKeyword(Long keywordId, KeywordRequest request) {
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new EntityNotFoundException("키워드를 찾을 수 없습니다: " + keywordId));

        String normalized = KeywordNormalizer.normalize(request.name());

        keywordRepository.findByNormalizedName(normalized)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(keywordId)) {
                        throw new IllegalArgumentException("이미 존재하는 키워드입니다.");
                    }
                });

        keyword.updateName(request.name().trim(), normalized);
        Keyword saved = keywordRepository.save(keyword);
        return new KeywordResponse(saved.getId(), saved.getDisplayName());
    }

    @Transactional
    public void deleteKeyword(Long keywordId) {
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new EntityNotFoundException("키워드를 찾을 수 없습니다: " + keywordId));
        keywordRepository.delete(keyword);
    }
}
