package com.example.shortudy.domain.shorts.service;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.category.repository.CategoryRepository;
import com.example.shortudy.domain.keyword.service.KeywordService;
import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUpdateRequest;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ShortsService {

    private static final Set<String> VALID_SORT_PROPERTIES = Set.of(
        "id", "title", "durationSec", "createdAt", "updatedAt"
    );
    
    private static final Set<String> EXTENDED_VALID_SORT_PROPERTIES = Set.of(
        "id", "title", "durationSec", "createdAt", "updatedAt",
        "videoUrl", "thumbnailUrl", "description"
    );

    private static final String DEFAULT_SORT_PROPERTY = "id";
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;

    private final ShortsRepository shortsRepository;
    private final CategoryRepository categoryRepository;
    private final KeywordService keywordService;

    public ShortsService(ShortsRepository shortsRepository, CategoryRepository categoryRepository, KeywordService keywordService) {
        this.shortsRepository = shortsRepository;
        this.categoryRepository = categoryRepository;
        this.keywordService = keywordService;
    }

    public Shorts findShortsWithDetails(Long shortsId) {
        return shortsRepository.findWithDetailsAndKeywordsById(shortsId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));
    }

    public Page<Shorts> getShortsEntityList(Pageable pageable) {
        if (isRandomSortRequested(pageable)) {
            return shortsRepository.findRandomPublishedShorts(pageable);
        }

        if (hasValidSortProperties(pageable)) {
            return shortsRepository.findByStatus(ShortsStatus.PUBLISHED, pageable);
        }

        return shortsRepository.findRandomPublishedShorts(pageable);
    }

    public Page<Shorts> getShortsEntityByCategory(Long categoryId, Pageable pageable) {
        Pageable safePageable = createSafePageable(pageable);
        return shortsRepository.findByCategoryIdAndStatus(categoryId, ShortsStatus.PUBLISHED, safePageable);
    }

    public Page<Shorts> getPopularShortsEntities(Integer days, Pageable pageable) {
        if (days == null || days <= 0) days = 30;
        if (days > 90) days = 90;
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return shortsRepository.findPopularShorts(since, pageable);
    }

    public Page<Shorts> getMyShortsEntities(Long userId, Pageable pageable) {
        Pageable safePageable = createSafePageable(pageable);
        return shortsRepository.findByUserId(userId, safePageable);
    }

    @Transactional
    public ShortsResponse updateShorts(Long shortsId, ShortsUpdateRequest request) {
        Shorts shorts = findShortsById(shortsId);
        Category category = findCategoryById(request.categoryId());
        
        validateUpdateRequest(request);
        
        shorts.updateShorts(
            request.title(),
            request.description(),
            request.thumbnailUrl(),
            category,
            request.status()
        );

        if (request.keywordNames() != null) {
            shorts.clearKeywords();
            request.keywordNames().forEach(k -> shorts.addKeyword(keywordService.getOrCreateKeyword(k)));
        }

        return ShortsResponse.of(shorts, 0L, shorts.getViewCount());
    }

    @Transactional
    public void deleteShorts(Long shortsId) {
        validateShortsExists(shortsId);
        shortsRepository.deleteById(shortsId);
    }

    private void validateShortsExists(Long shortsId) {
        if (!shortsRepository.existsById(shortsId)) {
            throw new BaseException(ErrorCode.SHORTS_NOT_FOUND);
        }
    }

    private Shorts findShortsById(Long shortsId) {
        return shortsRepository.findById(shortsId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));
    }


    private Category findCategoryById(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BaseException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private void validateUpdateRequest(ShortsUpdateRequest request) {
        if (request.durationSec() != null && request.durationSec() <= 0) {
            throw new BaseException(ErrorCode.SHORTS_DURATION_INVALID);
        }
    }

    private boolean isRandomSortRequested(Pageable pageable) {
        return !pageable.getSort().isSorted();
    }

    private boolean hasValidSortProperties(Pageable pageable) {
        return pageable.getSort().stream()
            .allMatch(order -> VALID_SORT_PROPERTIES.contains(order.getProperty()));
    }

    private Pageable createSafePageable(Pageable pageable) {
        if (isRandomSortRequested(pageable)) {
            return createDefaultPageable(pageable);
        }

        if (hasExtendedValidSortProperties(pageable)) {
            return pageable;
        }

        return createDefaultPageable(pageable);
    }

    private boolean hasExtendedValidSortProperties(Pageable pageable) {
        return pageable.getSort().stream()
            .allMatch(order -> EXTENDED_VALID_SORT_PROPERTIES.contains(order.getProperty()));
    }

    private Pageable createDefaultPageable(Pageable originalPageable) {
        return PageRequest.of(
            originalPageable.getPageNumber(),
            originalPageable.getPageSize(),
            Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_PROPERTY)
        );
    }
}
