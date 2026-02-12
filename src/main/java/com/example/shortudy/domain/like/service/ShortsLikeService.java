package com.example.shortudy.domain.like.service;

import com.example.shortudy.domain.comment.repository.CommentRepository;
import com.example.shortudy.domain.keyword.entity.Keyword;
import com.example.shortudy.domain.like.dto.LikeToggleResponse;
import com.example.shortudy.domain.like.dto.MyLikedShortsResponse;
import com.example.shortudy.domain.like.dto.ShortsLikeResponse;
import com.example.shortudy.domain.like.dto.SortStandard;
import com.example.shortudy.domain.like.entity.ShortsLike;
import com.example.shortudy.domain.like.repository.ShortsLikeRepository;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.config.S3Service;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 좋아요 서비스 레이어
 */
@Service
public class ShortsLikeService {

    private final ShortsLikeRepository shortsLikeRepository;
    private final ShortsRepository shortsRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    private final S3Service s3Service;

    public ShortsLikeService(
            ShortsLikeRepository shortsLikeRepository,
            ShortsRepository shortsRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            S3Service s3Service) {
        this.shortsRepository = shortsRepository;
        this.userRepository = userRepository;
        this.shortsLikeRepository = shortsLikeRepository;
        this.commentRepository = commentRepository;
        this.s3Service = s3Service;
    }

    /**
     * 좋아요 처리
     * 취소 : 데이터 삭제 및 카운트 감소 요청
     * 등록 : 데이터 저장 및 카운트 증가 요청
     * @param userId 유저 ID
     * @param shortsId 쇼츠 ID
     */
    @Transactional
    public LikeToggleResponse toggleLike(Long userId, Long shortsId) {
        Shorts shorts = shortsRepository.findById(shortsId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));
        Optional<ShortsLike> existingLike = shortsLikeRepository.findWithDeleted(userId, shortsId);

        if (existingLike.isPresent()) {
            ShortsLike like = existingLike.get();
            return toggleExistingLike(shorts, like);
        }

        return createNewLike(userId, shorts);
    }

    /**
     * 임의의 사용자가 누른 좋아요 숏츠 목록 처리
     * @param userId 사용자 ID
     * @return 해당하는 좋아요 숏츠 목록 리스트
     */
    @Transactional(readOnly = true)
    public Page<MyLikedShortsResponse> getMyLikedShorts(Long userId, String sort, Pageable pageable) {

        Page<ShortsLike> likes = switch(SortStandard.fromValue(sort)) {
            case LATEST -> shortsLikeRepository.findAllByUserIdWithDetailsLatest(userId, pageable);
            case POPULAR -> shortsLikeRepository.findAllByUserIdWithDetailsPopular(userId, pageable);
        };

        List<Long> shortsIds = likes.getContent().stream()
                .map(sl -> sl.getShorts().getId())
                .toList();

        Map<Long, Long> commentCountMap = commentRepository.countByShortsIds(shortsIds).stream()
                .collect(Collectors.toMap(
                        result -> (Long)result[0],
                        result -> (Long)result[1]
                ));

        return likes.map(like -> MyLikedShortsResponse.from(
            like.getShorts(),
            like.getShorts().getKeywords().stream().map(Keyword::getDisplayName).toList(),
            commentCountMap.getOrDefault(like.getShorts().getId(), 0L).intValue(),
            s3Service.getFileUrl(like.getUser().getProfileUrl())
        ));
    }

    /**
     * 좋아요가 존재할때의 토글 처리
     * @param shorts 좋아요 대상의 숏츠
     * @param like 존재하는 좋아요 엔티티
     * @return 좋아요 결과 응답 DTO
     */
    private LikeToggleResponse toggleExistingLike(Shorts shorts, ShortsLike like) {
        if(like.isDeleted()) {
            like.restore();
            shorts.incrementLikeCount();
            return new LikeToggleResponse(true, shorts.getLikeCount());
        }

        shortsLikeRepository.delete(like);
        shorts.decrementLikeCount();
        return new LikeToggleResponse(false, shorts.getLikeCount());
    }

    /**
     * 좋아요 객체 생성
     * @param userId 좋아요 누른 사용자
     * @param shorts 좋아요 눌러진 숏츠
     * @return 좋아요 결과 응답 DTO
     */
    private LikeToggleResponse createNewLike(Long userId, Shorts shorts) {
        User user = userRepository.getReferenceById(userId);
        shortsLikeRepository.save(ShortsLike.of(user, shorts));
        shorts.incrementLikeCount();
        return new LikeToggleResponse(true, shorts.getLikeCount());
    }

    /**
     * 특정 숏츠에 대한 좋아요 상태 조회
     * @param userId 사용자 ID
     * @param shortsId 숏츠 ID
     * @return 좋아요 상태 응답 DTO
     */
    @Transactional(readOnly = true)
    public ShortsLikeResponse getShortsLikeStatus(Long userId, Long shortsId) {
        boolean existShortsLike = shortsLikeRepository.existsByUserIdAndShortsId(userId, shortsId);

        return ShortsLikeResponse.from(
                shortsId,
                userId,
                existShortsLike
        );
    }
}
