package com.example.shortudy.domain.shorts.facade;

import com.example.shortudy.domain.comment.query.CommentCountProvider;
import com.example.shortudy.domain.like.entity.ShortsLike;
import com.example.shortudy.domain.like.repository.ShortsLikeRepository;
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
    private final ShortsLikeRepository shortsLikeRepository;

    /**
     * 상세 조회 (Facade)
     */
    public ShortsResponse getShortsDetails(Long shortsId, Long userId) {
        Shorts shorts = shortsService.findShortsWithDetails(shortsId);
        long commentCount = commentCountProvider.commentCount(shortsId);
        
        // 실시간 조회수 보정: DB 값 + Redis 미반영분
        Map<Long, Long> pendingCounts = redisShortsViewCountRepository.findPendingViewCounts();
        long realTimeViewCount = shorts.getViewCount() + pendingCounts.getOrDefault(shortsId, 0L);

        boolean isLiked = (userId != null) && shortsLikeRepository.existsByUserIdAndShortsId(userId, shortsId);
        
        return ShortsResponse.of(shorts, commentCount, realTimeViewCount, isLiked);
    }

    /**
     * 목록 조회 (Facade) - 페이징 처리 및 카운트 집계
     */
    public Page<ShortsResponse> getShortsList(Pageable pageable, Long userId) {
        Page<Shorts> shortsPage = shortsService.getShortsEntityList(pageable);
        return aggregateCounts(shortsPage, userId);
    }

    /**
     * 카테고리별 조회 (Facade)
     */
    public Page<ShortsResponse> getShortsByCategory(Long categoryId, Pageable pageable, Long userId) {
        Page<Shorts> shortsPage = shortsService.getShortsEntityByCategory(categoryId, pageable);
        return aggregateCounts(shortsPage, userId);
    }

    /**
     * 인기 숏츠 조회 (Facade)
     */
    public Page<ShortsResponse> getPopularShorts(Integer days, Pageable pageable, Long userId) {
        Page<Shorts> shortsPage = shortsService.getPopularShortsEntities(days, pageable);
        return aggregateCounts(shortsPage, userId);
    }

    /**
     * 내 쇼츠 조회 (Facade)
     */
    public Page<ShortsResponse> getMyShorts(Long userId, Pageable pageable) {
        Page<Shorts> shortsPage = shortsService.getMyShortsEntities(userId, pageable);
        return aggregateCounts(shortsPage, userId);
    }

    /**
     * 숏츠 목록에 대해 댓글수 및 실시간 조회수를 집계하여 DTO로 변환
     */
    private Page<ShortsResponse> aggregateCounts(Page<Shorts> shortsPage, Long userId) {
        List<Long> shortsIds = shortsPage.getContent().stream()
                .map(Shorts::getId)
                .toList();

        // 1. 댓글 수 일괄 조회 (Batch Query)
        Map<Long, Long> commentCountMap = commentCountProvider.commentCountByShortsIds(shortsIds);

        // 2. 실시간 조회수 일괄 조회 (Redis Multi-Get 지향)
        Map<Long, Long> pendingViewCounts = redisShortsViewCountRepository.findPendingViewCounts();

        // 3. 좋아요 여부 일괄 조회
        List<Long> likedShortsIds;
        if (userId != null && !shortsIds.isEmpty()) {
            likedShortsIds = shortsLikeRepository.findByUserIdAndShortsIdIn(userId, shortsIds).stream()
                    .map(like -> like.getShorts().getId())
                    .toList();
        } else {
            likedShortsIds = List.of();
        }

        return shortsPage.map(shorts -> {
            Long shortsId = shorts.getId();
            Long commentCount = commentCountMap.getOrDefault(shortsId, 0L);
            Long pendingView = pendingViewCounts.getOrDefault(shortsId, 0L);
            Long realTimeView = shorts.getViewCount() + pendingView;
            boolean isLiked = likedShortsIds.contains(shortsId);

            return ShortsResponse.of(shorts, commentCount, realTimeView, isLiked);
        });
    }

    private long calculateRealTimeViewCount(Long shortsId, Long dbViewCount) {
        Map<Long, Long> pendingCounts = redisShortsViewCountRepository.findPendingViewCounts();
        return dbViewCount + pendingCounts.getOrDefault(shortsId, 0L);
    }
}
