package com.example.shortudy.domain.like.entity;

import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@DisplayName("ShortsLike Entity 테스트")
class ShortsLikeTest {

    @Test
    @DisplayName("TC-SL-001: 유저와 숏츠 객체로 좋아요 엔티티를 생성한다")
    void shouldCreateEntity_WhenInputUserAndShorts() {
        // given
        User mockUser = mock(User.class);
        Shorts mockShorts = mock(Shorts.class);

        // when
        ShortsLike like = ShortsLike.of(mockUser, mockShorts);

        // then
        assertEquals(mockUser, like.getUser(), "엔티티의 유저 정보와 입력된 유저 정보가 같아야 합니다");
        assertEquals(mockShorts, like.getShorts(), "엔티티의 숏츠 정보와 입력된 숏츠 정보가 같아야 합니다");
    }

    @Test
    @DisplayName("TC-SL-002: 유저 정보가 없을 시 좋아요 엔티티 생성이 실패한다")
    void shouldNotCreateEntity_WhenInputShortsOnly() {
        // given
        Shorts mockShorts = mock(Shorts.class);

        // when, then
        assertThrows(
                NullPointerException.class,
                () -> ShortsLike.of(null, mockShorts),
                "유저 정보가 없으면 엔티티 생성이 실패합니다");
    }

    @Test
    @DisplayName("TC-SL-003: 숏츠 정보가 없을 시 좋아요 엔티티 생성이 실패한다")
    void shouldNotCreateEntity_WhenInputUserOnly() {
        // given
        User mockUser = mock(User.class);

        // when, then
        assertThrows(
                NullPointerException.class,
                () -> ShortsLike.of(mockUser, null),
                "숏츠 정보가 없으면 엔티티 생성이 실패합니다");
    }

}