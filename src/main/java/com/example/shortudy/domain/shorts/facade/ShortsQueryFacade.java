package com.example.shortudy.domain.shorts.facade;

import com.example.shortudy.domain.comment.query.CommentCountProvider;
import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.domain.shorts.view.repository.RedisShortsViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShortsQueryFacade {

    private final ShortsService shortsService;
    private final CommentCountProvider commentCountProvider;
    private final RedisShortsViewCountRepository redisShortsViewCountRepository;

    /**
     * 상세 조회 (Facade)
     */
    public ShortsResponse getShortsDetails(Long shortsId) {
        Shorts shorts = shortsService.findShortsWithDetails(shortsId);
        long commentCount = commentCountProvider.commentCount(shortsId);
        
        // 실시간 조회수 보정 (DB + Redis Pending)
        long realTimeViewCount = calculateRealTimeViewCount(shortsId, shorts.getViewCount());
        
        return ShortsResponse.of(shorts, commentCount, realTimeViewCount);
    }

    /**
     * 목록 조회 (Facade) - 페이징 처리 및 카운트 집계
     */
    public Page<ShortsResponse> getShortsList(Pageable pageable) {
        Page<Shorts> shortsPage = shortsService.getShortsEntityList(pageable);
        return aggregateCounts(shortsPage);
    }

    /**
     * 카테고리별 조회 (Facade)
     */
    public Page<ShortsResponse> getShortsByCategory(Long categoryId, Pageable pageable) {
        Page<Shorts> shortsPage = shortsService.getShortsEntityByCategory(categoryId, pageable);
        return aggregateCounts(shortsPage);
    }

    /**
     * 인기 숏츠 조회 (Facade)
     */
    public Page<ShortsResponse> getPopularShorts(Integer days, Pageable pageable) {
        Page<Shorts> shortsPage = shortsService.getPopularShortsEntities(days, pageable);
        return aggregateCounts(shortsPage);
    }

    /**
     * 내 쇼츠 조회 (Facade)
     */
    public Page<ShortsResponse> getMyShorts(Long userId, Pageable pageable) {
        Page<Shorts> shortsPage = shortsService.getMyShortsEntities(userId, pageable);
        return aggregateCounts(shortsPage);
    }

    /**
     * 숏츠 목록에 대해 댓글수 및 실시간 조회수를 집계하여 DTO로 변환
     */
    private Page<ShortsResponse> aggregateCounts(Page<Shorts> shortsPage) {
        List<Long> shortsIds = shortsPage.getContent().stream()
                .map(Shorts::getId)
                .toList();

        // 1. 댓글 수 일괄 조회 (Batch Query)
        Map<Long, Long> commentCountMap = commentCountProvider.commentCountByShortsIds(shortsIds);

        // 2. 실시간 조회수 일괄 조회 (Redis Multi-Get 지향)
        Map<Long, Long> pendingViewCounts = redisShortsViewCountRepository.findPendingViewCounts();

        return shortsPage.map(shorts -> {
            Long shortsId = shorts.getId();
            Long commentCount = commentCountMap.getOrDefault(shortsId, 0L);
            Long pendingView = pendingViewCounts.getOrDefault(shortsId, 0L);
            Long realTimeView = shorts.getViewCount() + pendingView;

            return ShortsResponse.of(shorts, commentCount, realTimeView);
        });
    }

    private long calculateRealTimeViewCount(Long shortsId, Long dbViewCount) {
        Map<Long, Long> pendingCounts = redisShortsViewCountRepository.findPendingViewCounts();
        return dbViewCount + pendingCounts.getOrDefault(shortsId, 0L);
    }
}
