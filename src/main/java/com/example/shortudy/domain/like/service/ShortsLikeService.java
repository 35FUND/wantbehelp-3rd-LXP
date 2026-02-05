package com.example.shortudy.domain.like.service;

import com.example.shortudy.domain.like.dto.LikeToggleResponse;
import com.example.shortudy.domain.like.entity.ShortsLike;
import com.example.shortudy.domain.like.repository.ShortsLikeRepository;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.shorts.service.ShortsService;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 좋아요 서비스 레이어
 */
@Service
public class ShortsLikeService {

    private final ShortsLikeRepository shortsLikeRepository;
    private final ShortsRepository shortsRepository;
    private final UserRepository userRepository;

    public ShortsLikeService(
            ShortsService shortsService,
            ShortsLikeRepository shortsLikeRepository,
            ShortsRepository shortsRepository,
            UserRepository userRepository) {
        this.shortsRepository = shortsRepository;
        this.userRepository = userRepository;
        this.shortsLikeRepository = shortsLikeRepository;
    }

    /**
     * 좋아요 토글
     * 취소 : 데이터 삭제 및 카운트 감소 요청
     * 등록 : 데이터 저장 및 카운트 증가 요청
     * @param userId 유저 ID
     * @param shortsId 쇼츠 ID
     */
    /* TODO: 고려 할 점
     * 1. 현재 shortRepository, userRepository를 직접 사용하고 있는데, 각 서비스의 서비스레이어에서 기능을 제공하도록 수정해야하는가
     * 2. 정책이 추가 될 게 있는지?
     */
    @Transactional
    public LikeToggleResponse toggleLike(Long userId, Long shortsId) {
        Shorts shorts = shortsRepository.findById(shortsId)
                .orElseThrow(() -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));
        Optional<ShortsLike> existingLike = shortsLikeRepository.findByUserIdAndShortsId(userId, shortsId);

        if (existingLike.isPresent()) {
            shortsLikeRepository.delete(existingLike.get());
            shorts.decrementLikeCount();

            return new LikeToggleResponse(false, shorts.getLikeCount());
        } else {
            User user = userRepository.getReferenceById(userId);
            shortsLikeRepository.save(ShortsLike.of(user, shorts));
            shorts.incrementLikeCount();

            return new LikeToggleResponse(true, shorts.getLikeCount());
        }
    }

    @Deprecated(since = "토글 체크 로직으로 삭제 예정")
    @Transactional
    public void like(Long userId, Long shortsId) {

        Shorts shorts = shortsRepository.findById(shortsId).orElseThrow(()
                -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));

        User user = userRepository.findById(userId).orElseThrow(()
                -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (shortsLikeRepository.existsByUserIdAndShortsId(userId, shortsId)) {
            throw new BaseException(ErrorCode.ALREADY_LIKE);
        }

        shortsLikeRepository.save(ShortsLike.of(user, shorts));
        shorts.incrementLikeCount(); // [추가] 카운트 증가
    }

    @Deprecated(since = "토글 체크 로직으로 삭제 예정")
    @Transactional
    public void unlike(Long userId, Long shortsId) {

        Shorts shorts = shortsRepository.findById(shortsId).orElseThrow(()
                -> new BaseException(ErrorCode.SHORTS_NOT_FOUND));

        User user = userRepository.findById(userId).orElseThrow(()
                -> new BaseException(ErrorCode.USER_NOT_FOUND));

        ShortsLike like = shortsLikeRepository.
                findByUserIdAndShortsId(user.getId(), shorts.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.ALREADY_UNLIKE));

        shortsLikeRepository.delete(like);
        shorts.decrementLikeCount(); // [추가] 카운트 감소
    }

}
