package com.example.shortudy.domain.keyword.service;

import com.example.shortudy.domain.keyword.dto.response.KeywordResponse;
import com.example.shortudy.domain.keyword.repository.KeywordRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
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
    @Transactional(readOnly = true)
    public List<KeywordResponse> getAllKeywords() {
        return keywordRepository.findAll().stream()
                .map(k -> new KeywordResponse(
                        k.getId(),
                        k.getDisplayName(),
                        k.getNormalizedName()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public KeywordResponse getKeyword(Long keywordId) {
        return keywordRepository.findById(keywordId)
                .map(k -> new KeywordResponse(
                        k.getId(),
                        k.getDisplayName(),
                        k.getNormalizedName()
                ))
                .orElseThrow(() -> new BaseException(ErrorCode.KEYWORD_NOT_FOUND));

    }
}
