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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
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

    private User user, user2;
    private Shorts shorts, shorts2;
    private Category category;

    @BeforeEach
    void setUp() {
        user = User.create("test@example.com", "password", "nickname", UserRole.USER, "");
        user2 = User.create("test2@example.com", "password", "nickname", UserRole.USER, "");
        em.persist(user);
        em.persist(user2);

        category = new Category("category");
        em.persist(category);

        shorts = new Shorts(user, category, "title", "description",
                "http://video.url", "http://thumbnail.url", 500, ShortsStatus.PUBLISHED);
        shorts2 = new Shorts(user, category, "title2", "description2",
                "http://video.url2", "http://thumbnail.url2", 700, ShortsStatus.PUBLISHED);

        em.persist(shorts);
        em.persist(shorts2);
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

    @Test
    @DisplayName("TC-SLR-005: 유저 ID로 좋아요 목록 조회 시 Shorts 정보와 함께 최신순으로 가져온다")
    void shouldGetDetailLatest_WhenFindAllByUserId() {
        // given
        shortsLikeRepository.save(ShortsLike.of(user, shorts));
        shortsLikeRepository.save(ShortsLike.of(user, shorts2));

        em.flush();
        em.clear();

        // when
        Page<ShortsLike> results = shortsLikeRepository.findAllByUserIdWithDetailsLatest(user.getId(), PageRequest.of(0, 10));

        // then
        assertEquals(2, results.getTotalElements(), "전체 데이터는 좋아요 한 숏츠의 갯수와 같아야 합니다");
        assertEquals(2, results.getContent().size(), "현재 페이지의 데이터 개수는 요청한 값과 같아야 합니다");
        assertEquals("title2", results.getContent().get(0).getShorts().getTitle(),"최신순으로 가져온 숏츠의 제목이어야 합니다");
        assertEquals("description2", results.getContent().get(0).getShorts().getDescription(), "최신순으로 가져온 숏츠의 설명이어야 합니다");

    }

    @Test
    @DisplayName("TC-SLR-006: 유저 ID로 좋아요 목록 조회 시 Shorts 정보와 함께 인기순으로 가져온다")
    void shouldGetDetailPopular_WhenFindAllByUserId() {
        // given
        shortsLikeRepository.save(ShortsLike.of(user, shorts));
        shortsLikeRepository.save(ShortsLike.of(user, shorts2));
        shortsLikeRepository.save(ShortsLike.of(user2, shorts2));

        em.flush();
        em.clear();

        // when
        Page<ShortsLike> results = shortsLikeRepository.findAllByUserIdWithDetailsPopular(user.getId(), PageRequest.of(0, 10));

        // then
        assertEquals(2, results.getTotalElements(), "전체 데이터는 좋아요 한 숏츠의 갯수와 같아야 합니다");
        assertEquals(2, results.getContent().size(), "현재 페이지의 데이터 개수는 요청한 값과 같아야 합니다");
        assertEquals("title2", results.getContent().get(0).getShorts().getTitle(),"인기순으로 가져온 숏츠의 제목이어야 합니다");
        assertEquals("description2", results.getContent().get(0).getShorts().getDescription(), "인기순으로 가져온 숏츠의 설명이어야 합니다");

    }
}