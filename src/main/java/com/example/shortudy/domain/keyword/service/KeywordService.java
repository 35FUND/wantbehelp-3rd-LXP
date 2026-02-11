package com.example.shortudy.domain.keyword.service;

import com.example.shortudy.domain.keyword.dto.response.KeywordResponse;
import com.example.shortudy.domain.keyword.entity.Keyword;
import com.example.shortudy.domain.keyword.repository.KeywordRepository;
import com.example.shortudy.domain.keyword.util.KeywordNormalizer;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public KeywordService(KeywordRepository keywordRepository) {
        this.keywordRepository = keywordRepository;
    }

    public List<KeywordResponse> getAllKeywords() {
        return keywordRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /*
    * TODO : 해당 API가 실제로 필요한지 검토 필요.
    *  필요 없으면 삭제 예정.
    * */
//    @Transactional(readOnly = true)
//    public KeywordResponse getKeyword(Long keywordId) {
//        return keywordRepository.findById(keywordId)
//                .map(this::toResponse)
//                .orElseThrow(() -> new BaseException(ErrorCode.KEYWORD_NOT_FOUND));
//    }

    @Deprecated(since = "해당 메서드는 제거될 가능성이 있습니다.")
    public List<KeywordResponse> searchKeywords(String q) {
        if (q == null || q.isBlank()) return List.of();

        String trimmed = q.trim();
        if (trimmed.isBlank()) return List.of();

        String normalizedForSearch = KeywordNormalizer.normalizeForSearch(trimmed);
        if (normalizedForSearch.isEmpty()) return List.of();

        String normalizedNoSpace = normalizedForSearch.replaceAll("\\s+", "");

        List<Keyword> keywords = keywordRepository.searchKeyword(trimmed, normalizedNoSpace);

        return keywords.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public KeywordResponse createKeyword(String displayName) {
        String normalized = KeywordNormalizer.normalize(displayName);

        if (normalized == null || normalized.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }

        Optional<Keyword> existing = keywordRepository.findByNormalizedName(normalized);
        if (existing.isPresent()) {
            throw new BaseException(ErrorCode.DUPLICATE_KEYWORD);
        }

        Keyword keyword = new Keyword(displayName, normalized);
        Keyword savedKeyword = keywordRepository.save(keyword);
        return toResponse(savedKeyword);
    }

    @Transactional
    public KeywordResponse updateKeyword(Long id, String displayName) {
        Keyword existing = keywordRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.KEYWORD_NOT_FOUND));

        String normalized = KeywordNormalizer.normalize(displayName);

        if (normalized == null || normalized.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }

        Optional<Keyword> byNormalized = keywordRepository.findByNormalizedName(normalized);
        if (byNormalized.isPresent() && !byNormalized.get().getId().equals(id)) {
            throw new BaseException(ErrorCode.DUPLICATE_KEYWORD);
        }

        existing.updateName(displayName, normalized);
        Keyword saved = keywordRepository.save(existing);
        return toResponse(saved);
    }

    @Transactional
    public void deleteKeyword(Long id) {
        if (!keywordRepository.existsById(id)) {
            throw new BaseException(ErrorCode.KEYWORD_NOT_FOUND);
        }
        keywordRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Keyword getValidKeyword(String displayName) {
        String normalized = KeywordNormalizer.normalize(displayName);

        if (normalized == null || normalized.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_INPUT);
        }

        return keywordRepository.findByNormalizedName(normalized)
                .orElseThrow(() -> new BaseException(ErrorCode.KEYWORD_NOT_FOUND, "존재하지 않는 키워드입니다: " + displayName));
    }

    private KeywordResponse toResponse(Keyword k) {
        return new KeywordResponse(k.getId(), k.getDisplayName(), k.getNormalizedName());
    }
}
