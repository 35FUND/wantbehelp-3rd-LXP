package com.example.shortudy.domain.like.service;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.like.dto.LikeToggleResponse;
import com.example.shortudy.domain.like.entity.ShortsLike;
import com.example.shortudy.domain.like.repository.ShortsLikeRepository;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.entity.UserRole;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.config.JpaAuditConfig;
import com.example.shortudy.global.error.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import({ShortsLikeService.class, JpaAuditConfig.class})
@DisplayName("Like Service 테스트")
class ShortsLikeServiceTest {

    @Autowired
    private ShortsLikeService shortsLikeService;

    @Autowired
    private ShortsLikeRepository shortsLikeRepository;

    @Autowired
    private ShortsRepository shortsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Shorts shorts;

    @BeforeEach
    void setUp() {
        user = User.create("test@example.com", "password", "nickname", UserRole.USER, "");
        userRepository.save(user);

        Category category = new Category("category");
        em.persist(category);

        shorts = new Shorts(user, category, "title", "description",
                "http://video.url", "http://thumbnail.url", 500, ShortsStatus.PUBLISHED);
        shortsRepository.save(shorts);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("TC-SLS-001: 좋아요가 없는 상태에서 호출하면 새로운 좋아요가 생성되고, 카운트 1이 증가한다")
    void shouldCreateLikeAndPlusCountOne_WhenLikeNotExists() {

        // when
        LikeToggleResponse response = shortsLikeService.toggleLike(user.getId(), shorts.getId());
        Optional<ShortsLike> savedLike = shortsLikeRepository.findWithDeleted(user.getId(), shorts.getId());

        // then
        assertTrue(response.isLiked(), "좋아요가 등록상태야 합니다");
        assertEquals(1, response.likeCount(), "좋아요가 취소되면 총 카운트가 0이어야 합니다");

        assertTrue(savedLike.isPresent(), "등록된 좋아요가 Soft delete가 아닌 상태로 조회되어야 합니다.");
    }

    @Test
    @DisplayName("TC-SLS-002: 이미 좋아요가 있는 상태에서 호출하면 좋아요를 취소(Soft delete)한다")
    void shouldSoftDeleteLike_WhenLikeExists() {
        // given
        shortsLikeService.toggleLike(user.getId(), shorts.getId());
        em.flush();
        em.clear();

        // when
        LikeToggleResponse response = shortsLikeService.toggleLike(user.getId(), shorts.getId());

        // then
        assertFalse(response.isLiked(), "좋아요가 취소상태야 합니다");
        assertEquals(0, response.likeCount(), "좋아요가 취소되면 총 카운트가 0이어야 합니다");

        Optional<ShortsLike> activeLike = shortsLikeRepository.findByUserIdAndShortsId(user.getId(), shorts.getId());
        assertTrue(activeLike.isEmpty(), "삭제 된 좋아요는 조회될 수 없습니다");

        Optional<ShortsLike> deletedLike = shortsLikeRepository.findWithDeleted(user.getId(), shorts.getId());
        assertTrue(deletedLike.isPresent(), "soft delete 된 좋아요가 조회되어야 합니다");
        assertTrue(deletedLike.get().isDeleted(), "soft delete 된 좋아요는 삭제 날짜가 기록되어야 합니다");
    }

    @Test
    @DisplayName("TC-SLS-003: 좋아요 재등록(복구): 취소했던 좋아요를 다시 누르면 새로운 행이 생기지 않고 기존 데이터가 복구된다")
    void shouldRestoreLike_WhenClickDeletedLike() {
        // given
        shortsLikeService.toggleLike(user.getId(), shorts.getId()); // 등록
        shortsLikeService.toggleLike(user.getId(), shorts.getId()); // 취소
        em.flush();
        em.clear();

        // when
        LikeToggleResponse response = shortsLikeService.toggleLike(user.getId(), shorts.getId());

        // then
        assertTrue(response.isLiked(), "좋아요가 복구되어야 합니다");
        assertEquals(1, response.likeCount(), "좋아요 총 횟수가 1 증가해야 합니다");

        Optional<ShortsLike> restoredLike = shortsLikeRepository.findWithDeleted(user.getId(), shorts.getId());
        assertTrue(restoredLike.isPresent(), "복구 된 좋아요는 조회되어야 합니다");
        assertFalse(restoredLike.get().isDeleted(), "복구 된 좋아요의 삭제 날짜는 null이어야 합니다");
    }

    @Test
    @DisplayName("TC-SLS-004: 존재하지 않는 숏츠에 좋아요를 시도하면 예외가 발생한다")
    void shouldThrowException_WhenShortsNotFound() {
        // given
        Long notExistingId = 9999L; // 실제 DB에 없을 만한 번호 사용

        // when & then
        assertThrows(BaseException.class,
                () -> shortsLikeService.toggleLike(user.getId(), notExistingId),
                "존재하지 않는 숏츠에 좋아요를 할 수 없습니다");
    }

}