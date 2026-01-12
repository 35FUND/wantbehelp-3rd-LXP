package com.example.shortudy.domain.shorts.service;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.category.repository.CategoryRepository;
import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUpdateRequest;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ShortsService {

    private final ShortsRepository shortsRepository;
    private final CategoryRepository categoryRepository;

    public ShortsService(ShortsRepository shortsRepository, CategoryRepository categoryRepository) {
        this.shortsRepository = shortsRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public ShortsResponse getShortsDetails(Long shortsId) {
        Shorts shorts = shortsRepository.findWithDetailsById(shortsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 숏츠입니다."));

        return ShortsResponse.from(shorts);
    }

    /**
     * 숏폼 상세 조회 (페이징)
     * 요청한 ID의 영상이 포함된 페이징 데이터 반환 (size=20)
     */
    @Transactional(readOnly = true)
    public Page<ShortsResponse> getShortsDetailsWithPaging(Long shortsId, Pageable pageable) {
        // 요청한 영상이 존재하는지 확인
        shortsRepository.findWithDetailsById(shortsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 숏츠입니다."));

        // 안전한 정렬 처리
        Pageable safePageable = createSafePageable(pageable);

        // 전체 목록을 페이징으로 조회
        return shortsRepository.findAll(safePageable).map(ShortsResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ShortsResponse> getShortsList(Pageable pageable) {
        // sort 파라미터가 없으면 → 랜덤 조회 (메인 페이지)
        // sort 파라미터가 있고 유효하면 → 해당 정렬 적용

        if (!pageable.getSort().isSorted()) {
            // 정렬 파라미터 없음 → 랜덤 조회
            return shortsRepository.findAllRandom(pageable).map(ShortsResponse::from);
        }

        // 유효한 정렬 속성 목록
        java.util.Set<String> validProperties = java.util.Set.of(
            "id", "title", "durationSec", "createdAt", "updatedAt"
        );

        // 정렬 속성 검증
        boolean hasInvalidProperty = pageable.getSort().stream()
            .anyMatch(order -> !validProperties.contains(order.getProperty()));

        if (hasInvalidProperty) {
            // 잘못된 정렬 속성 → 랜덤 조회
            return shortsRepository.findAllRandom(pageable).map(ShortsResponse::from);
        }

        // 유효한 정렬 → 해당 정렬로 조회
        return shortsRepository.findAll(pageable).map(ShortsResponse::from);
    }

    /**
     * 안전한 Pageable 생성 - 유효하지 않은 정렬 속성 방지
     */
    private Pageable createSafePageable(Pageable pageable) {
        // 정렬이 없으면 기본 정렬 적용
        if (!pageable.getSort().isSorted()) {
            return org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")
            );
        }

        // 유효한 정렬 속성 목록
        java.util.Set<String> validProperties = java.util.Set.of(
            "id", "title", "durationSec", "createdAt", "updatedAt",
            "videoUrl", "thumbnailUrl", "description"
        );

        // 정렬 속성 검증
        boolean hasInvalidProperty = pageable.getSort().stream()
            .anyMatch(order -> !validProperties.contains(order.getProperty()));

        if (hasInvalidProperty) {
            // 잘못된 정렬 속성이 있으면 기본 정렬 사용
            return org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")
            );
        }

        return pageable;
    }

    @Transactional
    public ShortsResponse updateShorts(Long shortsId, ShortsUpdateRequest request){

        Shorts shorts = shortsRepository.findById(shortsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 숏츠입니다."));

        if(request.durationSec() != null && request.durationSec() <= 0){
            throw new IllegalArgumentException("영상 길이는 1초 이상이어야 합니다.");
        }

        Category category = null;
        if(request.categoryId() != null){
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
        }
        shorts.updateShorts(
                request.title(),
                request.description(),
                request.thumbnailUrl(),
                category,
                request.status()
        );

        return ShortsResponse.from(shorts);
    }

    @Transactional
    public void deleteShorts(Long shortsId){

        if(!shortsRepository.existsById(shortsId)){
            throw new NoSuchElementException("존재하지 않는 숏츠입니다.");
        }

        shortsRepository.deleteById(shortsId);
    }
}
