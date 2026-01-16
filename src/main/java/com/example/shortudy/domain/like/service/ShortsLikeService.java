package com.example.shortudy.domain.like.service;

import com.example.shortudy.domain.like.entity.ShortsLike;
import com.example.shortudy.domain.like.repository.ShortsLikeRepository;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortsLikeService {

    private final ShortsLikeRepository shortsLikeRepository;
    private final ShortsRepository shortsRepository;
    private final UserRepository userRepository;

    public ShortsLikeService(ShortsLikeRepository shortsLikeRepository, ShortsRepository shortsRepository, UserRepository userRepository) {
        this.shortsRepository = shortsRepository;
        this.userRepository = userRepository;
        this.shortsLikeRepository = shortsLikeRepository;
    }

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
