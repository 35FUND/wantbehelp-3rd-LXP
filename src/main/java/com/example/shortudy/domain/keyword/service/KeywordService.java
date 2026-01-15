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
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public KeywordService(KeywordRepository keywordRepository) {
        this.keywordRepository = keywordRepository;
    }
    @Transactional(readOnly = true)
    public List<KeywordResponse> getAllKeywords() {
        return keywordRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    /*
    * this:toResponse
    * 1. 책임분리
    * 2. 재사용 및 중복 제거
    * 3. 엔티티 캡슐화(불변성 유지)
    * 4. DTO 사용의 장점(안전한 API 응답)
    * 5. 트랜잭션 설정 최적화
    * 6. 예외 처리 명확화
    * 7. 가독성 및 유지보수성 향상
    * */

    @Transactional(readOnly = true)
    public KeywordResponse getKeyword(Long keywordId) {
        return keywordRepository.findById(keywordId)
                .map(this::toResponse)
                .orElseThrow(() -> new BaseException(ErrorCode.KEYWORD_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<KeywordResponse> searchKeywords(String q) {
        if (q == null || q.isBlank()) return List.of();

        String trimmed = q.trim();
        if (trimmed.isBlank()) return List.of();

        // 검색용 정규화 호출 — 여기서 normalizedForSearch를 정의함
        String normalizedForSearch = KeywordNormalizer.normalizeForSearch(trimmed);
        if (normalizedForSearch.isEmpty()) return List.of();

        // 저장된 normalizedName은 내부 공백을 허용하지 않으므로 검색용 정규화 결과의 공백을 제거해서 매칭
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
            throw new BaseException(ErrorCode.SPACE_ONLY_KEYWORD);
        }

        // 중복 확인 (repository에 findByNormalizedName 또는 existsByNormalizedName 추가 필요)
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
            throw new BaseException(ErrorCode.SPACE_ONLY_KEYWORD);
        }

        // 같은 normalized를 가진 다른 엔티티가 있는지 확인
        Optional<Keyword> byNormalized = keywordRepository.findByNormalizedName(normalized);
        if (byNormalized.isPresent() && !byNormalized.get().getId().equals(id)) {
            throw new BaseException(ErrorCode.DUPLICATE_KEYWORD);
        }

        // 엔티티의 상태 갱신 메서드 사용
        existing.updateName(displayName, normalized);
        Keyword saved = keywordRepository.save(existing);
        return toResponse(saved);
    }

        @Transactional
        public void deleteKeyword (Long id){
            if (!keywordRepository.existsById(id)) {
                throw new BaseException(ErrorCode.KEYWORD_NOT_FOUND);
            }
            keywordRepository.deleteById(id);
        }

        private KeywordResponse toResponse (Keyword k){
            return new KeywordResponse(k.getId(), k.getDisplayName(), k.getNormalizedName());
        }
    }
