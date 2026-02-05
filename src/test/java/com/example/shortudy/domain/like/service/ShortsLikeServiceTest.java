package com.example.shortudy.domain.like.service;

import com.example.shortudy.domain.like.dto.LikeToggleResponse;
import com.example.shortudy.domain.like.entity.ShortsLike;
import com.example.shortudy.domain.like.repository.ShortsLikeRepository;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.error.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Like Service 테스트")
class ShortsLikeServiceTest {

    @InjectMocks
    private ShortsLikeService shortsLikeService;

    @Mock
    private ShortsLikeRepository shortsLikeRepository;

    @Mock
    private ShortsRepository shortsRepository;

    @Mock
    private UserRepository userRepository;

    private final Long userId = 1L;
    private final Long shortsId = 100L;
    private Shorts mockShorts;

    @BeforeEach
    void setUp() {
        mockShorts = mock(Shorts.class);
    }

    @Test
    @DisplayName("TC-SLS-001: 좋아요가 없는 상태에서 호출하면 새로운 좋아요를 등록한다")
    void shouldCreateLike_WhenLikeNotExists() {
        // given
        given(shortsRepository.findById(shortsId)).willReturn(Optional.of(mockShorts));
        given(shortsLikeRepository.findByUserIdAndShortsId(userId, shortsId)).willReturn(Optional.empty());
        given(userRepository.getReferenceById(userId)).willReturn(mock(User.class));
        given(mockShorts.getLikeCount()).willReturn(1);

        // when
        LikeToggleResponse response = shortsLikeService.toggleLike(userId, shortsId);

        // then
        assertTrue(response.isLiked(), "좋아요가 등록상태야 합니다");
        assertEquals(1, response.likeCount(), "좋아요가 취소되면 총 카운트가 0이어야 합니다");

        // 중요: save가 호출되었는지, 카운트 증가 메서드가 실행되었는지 확인
        verify(shortsLikeRepository, times(1)).save(any(ShortsLike.class));
        verify(mockShorts, times(1)).incrementLikeCount();
    }

    @Test
    @DisplayName("TC-SLS-002: 이미 좋아요가 있는 상태에서 호출하면 좋아요를 취소(삭제)한다")
    void shouldDeleteLike_WhenLikeExists() {
        // given
        ShortsLike existingLike = mock(ShortsLike.class);
        given(shortsRepository.findById(shortsId)).willReturn(Optional.of(mockShorts));
        given(shortsLikeRepository.findByUserIdAndShortsId(userId, shortsId)).willReturn(Optional.of(existingLike));
        given(mockShorts.getLikeCount()).willReturn(0);

        // when
        LikeToggleResponse response = shortsLikeService.toggleLike(userId, shortsId);

        // then
        assertFalse(response.isLiked(), "좋아요가 취소상태야 합니다");
        assertEquals(0, response.likeCount(), "좋아요가 취소되면 총 카운트가 0이어야 합니다");

        // 중요: delete가 호출되었는지, 카운트 감소 메서드가 실행되었는지 확인
        verify(shortsLikeRepository, times(1)).delete(existingLike);
        verify(mockShorts, times(1)).decrementLikeCount();
    }

    @Test
    @DisplayName("TC-SLS-003: 존재하지 않는 숏츠에 좋아요를 시도하면 예외가 발생한다")
    void shouldThrowException_WhenShortsNotFound() {
        // given
        given(shortsRepository.findById(shortsId)).willReturn(Optional.empty());

        // when & then
        assertThrows(BaseException.class,
                () -> shortsLikeService.toggleLike(userId, shortsId),
                "존재하지 않는 숏츠에 좋아요를 할 수 없습니다");
    }

}