package com.example.shortudy.domain.like.repository;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.like.entity.ShortsLike;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.entity.UserRole;
import com.example.shortudy.global.config.JpaAuditConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaAuditConfig.class)
@DisplayName("Like Repository 테스트")
class ShortsLikeRepositoryTest {

    @Autowired
    private ShortsLikeRepository shortsLikeRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Shorts shorts;
    private Category category;

    @BeforeEach
    void setUp() {
        user = User.create("test@example.com", "password", "nickname", UserRole.USER, "");
        em.persist(user);

        category = new Category("category");
        em.persist(category);

        shorts = new Shorts(user, category, "title", "description",
                "http://video.url", "http://thumbnail.url", 500, ShortsStatus.PUBLISHED);

        em.persist(shorts);
        em.persist(user);
        em.flush();
    }

    @Test
    @DisplayName("TC-SLR-001: 좋아요 저장 시 생성 시간이 자동으로 기록된다")
    void shouldRecordCreatedAt_WhenSaveLike() {
        // given
        ShortsLike like = ShortsLike.of(user, shorts);

        // when
        ShortsLike savedLike = shortsLikeRepository.save(like);
        em.flush();

        // then
        assertNotNull(savedLike.getCreatedAt());
        assertTrue(savedLike.getCreatedAt().isBefore(LocalDateTime.now()) ||
                savedLike.getCreatedAt().isEqual(LocalDateTime.now()));
    }

    @Test
    @Disabled("서비스 로직에서 복구(restore)를 수행하므로, DB 제약조건 위반 상황이 아님")
    @DisplayName("TC-SLR-002: 동일한 유저가 같은 영상에 중복 좋아요를 할 경우 DB 제약조건에 의해 에러가 발생한다")
    void shouldThrowException_WhenDuplicateLike() {
        // given
        ShortsLike firstLike = ShortsLike.of(user, shorts);
        shortsLikeRepository.save(firstLike);
        em.flush();

        // when & then
        ShortsLike secondLike = ShortsLike.of(user, shorts);

        // Unique 제약조건 위반으로 예외 발생 (DataIntegrityViolationException)
        assertThrows(DataIntegrityViolationException.class, () -> {
            shortsLikeRepository.save(secondLike);
            em.flush();
        });
    }

    @Test
    @DisplayName("TC-SLR-003: 유저와 숏츠 정보를 통해 좋아요 여부를 정확히 찾아온다")
    void shouldFindLike_ByUserIdAndShortsId() {
        // given
        ShortsLike like = ShortsLike.of(user, shorts);
        shortsLikeRepository.save(like);
        em.flush();
        em.clear(); // 1차 캐시 비우기

        // when
        Optional<ShortsLike> foundLike = shortsLikeRepository.findByUserIdAndShortsId(user.getId(), shorts.getId());

        // then
        assertTrue(foundLike.isPresent());
        assertEquals(user.getId(), foundLike.get().getUser().getId());
        assertEquals(shorts.getId(), foundLike.get().getShorts().getId());
    }

    @Test
    @DisplayName("TC-SLR-004: Soft Delete된 데이터가 있을 때, 다시 저장하면 기존 ID를 유지하며 복구된다")
    void shouldRestoreAndKeepId_WhenSavingDeletedLike() {
        // 1. Given: 좋아요 생성 후 Soft Delete 상태로 만듦
        ShortsLike like = ShortsLike.of(user, shorts);
        shortsLikeRepository.save(like);
        em.flush();

        // Soft Delete 실행 (이때 deleted_at에 값이 들어감)
        shortsLikeRepository.delete(like);
        em.flush();
        em.clear(); // 영속성 컨텍스트 비우기

        // 삭제된 데이터의 ID와 상태 확인
        ShortsLike deletedLike = shortsLikeRepository.findWithDeleted(user.getId(), shorts.getId())
                .orElseThrow();
        Long originalId = deletedLike.getId();
        assertTrue(deletedLike.isDeleted(), "먼저 삭제된 상태여야 합니다.");

        // 2. When: 복구 로직 실행 (상태를 바꾸고 다시 save)
        deletedLike.restore(); // deletedAt = null
        shortsLikeRepository.save(deletedLike);
        em.flush();
        em.clear();

        // 3. Then: 최종 상태 검증
        ShortsLike result = shortsLikeRepository.findWithDeleted(user.getId(), shorts.getId())
                .orElseThrow();

        assertFalse(result.isDeleted(), "복구 후에는 deletedAt이 NULL이어야 합니다.");
        assertEquals(originalId, result.getId(), "새로운 행이 생성되지 않고 기존 ID가 유지되어야 합니다.");
    }
}