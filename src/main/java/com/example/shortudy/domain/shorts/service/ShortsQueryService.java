package com.example.shortudy.domain.shorts.service;

import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsStatusDescriptionResponse;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsInspectionResults;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.shorts.repository.ShortsInspectionResultsRepository;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.shorts.view.repository.RedisShortsViewCountRepository;
import com.example.shortudy.global.config.S3Service;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 숏츠 조회 전용 서비스
 * 복잡한 집계 쿼리는 Repository에서 Object[]로 조회 후 DTO로 변환하여 반환합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShortsQueryService {

    private final ShortsRepository shortsRepository;
    private final ShortsInspectionResultsRepository shortsInspectionResultsRepository;
    private final RedisShortsViewCountRepository redisShortsViewCountRepository;
    private final S3Service s3Service;

    /**
     * 상세 조회 - DB 집계 데이터와 Redis 실시간 조회수를 통합하여 반환합니다.
     */
    public ShortsResponse getShortsDetails(Long shortsId, Long userId) {
        Object[] result = shortsRepository.findShortsWithCounts(shortsId, userId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));
        
        return mergeRealTimeViewCount(toShortsResponse(result));
    }

    /**
     * 목록 조회 - 발행된 숏츠 목록을 집계 데이터와 함께 조회합니다.
     */
    public Page<ShortsResponse> getShortsList(Pageable pageable, Long userId) {
        Page<Object[]> results = shortsRepository.findShortsPageWithCounts(ShortsStatus.PUBLISHED, userId, pageable);
        return mergeRealTimeViewCounts(results.map(this::toShortsResponse));
    }

    /**
     * 카테고리별 조회 - 특정 카테고리의 숏츠 목록을 집계 데이터와 함께 조회합니다.
     */
    public Page<ShortsResponse> getShortsByCategory(Long categoryId, Pageable pageable, Long userId) {
        Page<Object[]> results = shortsRepository.findByCategoryWithCounts(categoryId, ShortsStatus.PUBLISHED, userId, pageable);
        return mergeRealTimeViewCounts(results.map(this::toShortsResponse));
    }

    /**
     * 인기 숏츠 조회 - 최근 N일간의 인기 숏츠를 집계 데이터와 함께 조회합니다.
     */
    public Page<ShortsResponse> getPopularShorts(Integer days, Pageable pageable, Long userId) {
        if (days == null || days <= 0) days = 30;
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Page<Object[]> results = shortsRepository.findPopularWithCounts(since, userId, pageable);
        return mergeRealTimeViewCounts(results.map(this::toShortsResponse));
    }

    /**
     * 내 쇼츠 조회 - 내가 작성한 숏츠 목록을 집계 데이터와 함께 조회합니다.
     */
    public Page<ShortsResponse> getMyShorts(Long userId, Pageable pageable) {
        Page<Object[]> results = shortsRepository.findMyShortsWithCounts(userId, pageable);
        return mergeRealTimeViewCounts(results.map(this::toShortsResponse));
    }

    /**
     * Repository의 Object[] 결과를 ShortsResponse DTO로 변환합니다.
     */
    private ShortsResponse toShortsResponse(Object[] result) {
        Shorts shorts = (Shorts) result[0];
        Long commentCount = (Long) result[1];
        Boolean isLiked = (Boolean) result[2];
        return ShortsResponse.of(shorts, commentCount, shorts.getViewCount(), isLiked);
    }

    /**
     * 페이지 단위로 실시간 정보(조회수, 프로필 URL 등)를 통합합니다.
     */
    private Page<ShortsResponse> enrichAll(Page<ShortsResponse> responses) {
        Map<Long, Long> pendingCounts = redisShortsViewCountRepository.findPendingViewCounts();
        return responses.map(resp -> {
            Long pending = pendingCounts.getOrDefault(resp.shortsId(), 0L);
            return enrich(resp, resp.viewCount() + pending);
        });
    }

    /**
     * 단건에 대해 실시간 정보(조회수, 프로필 URL 등)를 통합합니다.
     */
    private ShortsResponse enrich(ShortsResponse response) {
        Map<Long, Long> pendingCounts = redisShortsViewCountRepository.findPendingViewCounts();
        Long pending = pendingCounts.getOrDefault(response.shortsId(), 0L);
        return enrich(response, response.viewCount() + pending);
    }

    /**
     * 실시간 데이터가 반영된 새로운 DTO를 생성합니다.
     */
    private ShortsResponse enrich(ShortsResponse original, long realTimeViewCount) {
        String fullProfileUrl = s3Service.getFileUrl(original.userProfileUrl());
        
        return new ShortsResponse(
                original.shortsId(), original.title(), original.description(),
                original.videoUrl(), original.thumbnailUrl(), original.durationSec(),
                original.status(), original.visibility(), original.userId(), original.userNickname(),
                fullProfileUrl, original.categoryId(), original.categoryName(),
                original.keywords(), realTimeViewCount, original.likeCount(),
                original.commentCount(), original.createdAt(), original.updatedAt(),
                original.isLiked()
        );
    }
}
