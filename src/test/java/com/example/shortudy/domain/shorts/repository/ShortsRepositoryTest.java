package com.example.shortudy.domain.shorts.repository;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.comment.entity.Comment;
import com.example.shortudy.domain.like.entity.ShortsLike;
import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.entity.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ShortsRepositoryTest {

    @Autowired
    private ShortsRepository shortsRepository;

    @Autowired
    private EntityManager em;

    private User user;
    private Category category;
    private Shorts shorts;

    @BeforeEach
    void setUp() {
        // 1. 유저 및 카테고리 생성
        user = User.create("test@test.com", "password", "nickname", UserRole.USER, "profile");
        em.persist(user);

        category = new Category("Test Category");
        em.persist(category);

        // 2. 기본 쇼츠 생성 (PUBLISHED)
        shorts = new Shorts(user, category, "Title", "Description", "http://video.com", "http://thumb.com", 60, ShortsStatus.PUBLISHED);
        em.persist(shorts);

        // 3. 연관 데이터 (댓글 2개, 좋아요 1개)
        em.persist(Comment.create(user, shorts, "Comment 1"));
        em.persist(Comment.create(user, shorts, "Comment 2"));
        em.persist(ShortsLike.of(user, shorts));

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("상세 조회 통합 쿼리 검증 (DTO)")
    void findResponseByIdTest() {
        Optional<ShortsResponse> response = shortsRepository.findResponseById(shorts.getId(), user.getId());
        
        assertThat(response).isPresent();
        assertThat(response.get().commentCount()).isEqualTo(2L);
        assertThat(response.get().isLiked()).isTrue();
        assertThat(response.get().userNickname()).isEqualTo("nickname");
    }

    @Test
    @DisplayName("카테고리별 DTO 목록 조회 검증")
    void findResponsesByCategoryIdAndStatusTest() {
        Page<ShortsResponse> responses = shortsRepository.findResponsesByCategoryIdAndStatus(
                category.getId(), ShortsStatus.PUBLISHED, user.getId(), PageRequest.of(0, 10));
        
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).categoryName()).isEqualTo("Test Category");
    }

    @Test
    @DisplayName("인기 숏츠(기간 필터링) DTO 조회 검증")
    void findPopularResponsesTest() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        Page<ShortsResponse> responses = shortsRepository.findPopularResponses(since, user.getId(), PageRequest.of(0, 10));
        
        assertThat(responses.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("내 숏츠 DTO 목록 조회 검증")
    void findMyResponsesTest() {
        Page<ShortsResponse> responses = shortsRepository.findMyResponses(user.getId(), PageRequest.of(0, 10));
        
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).userId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("조회수 일괄 업데이트(Bulk Update) 검증")
    void updateViewCountTest() {
        // given
        Long increment = 50L;
        
        // when
        shortsRepository.updateViewCount(shorts.getId(), increment);
        em.flush();
        em.clear();
        
        // then
        Shorts updated = shortsRepository.findById(shorts.getId()).get();
        assertThat(updated.getViewCount()).isEqualTo(increment); // 초기값 0 + 50
    }

    @Test
    @DisplayName("랜덤 숏츠 조회(Native Query) 검증")
    void findRandomPublishedShortsTest() {
        Page<Shorts> randoms = shortsRepository.findRandomPublishedShorts(PageRequest.of(0, 10));
        
        assertThat(randoms.getContent()).isNotEmpty();
        assertThat(randoms.getContent().get(0).getStatus()).isEqualTo(ShortsStatus.PUBLISHED);
    }

    @Test
    @DisplayName("추천 후보 조회(자기제외 + 랜덤) 검증")
    void findRecommendationCandidatesTest() {
        // given: 추천을 위해 다른 쇼츠 하나 더 생성
        User user2 = User.create("test2@test.com", "pass", "nick2", UserRole.USER, "p2");
        em.persist(user2);
        Shorts otherShorts = new Shorts(user2, category, "Other Video", "Desc", "https://video2.com", "https://thumb2.com", 30, ShortsStatus.PUBLISHED);
        em.persist(otherShorts);
        em.flush();

        // when: 현재 shorts(ID)를 제외한 추천 후보 조회
        List<Shorts> candidates = shortsRepository.findRecommendationCandidates(shorts.getId(), ShortsStatus.PUBLISHED, PageRequest.of(0, 10));

        // then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).extracting(Shorts::getId).doesNotContain(shorts.getId());
        assertThat(candidates).extracting(Shorts::getId).contains(otherShorts.getId());
    }

    @Test
    @DisplayName("Keywords 페치 조인 엔티티 조회 검증")
    void findWithDetailsAndKeywordsByIdTest() {
        // given: 키워드 데이터는 ShortsService의 도움 없이 직접 매핑 확인이 어려우므로 
        // 쿼리 구문 오류가 없는지, 기본 페치 조인이 작동하는지 위주로 확인
        Optional<Shorts> result = shortsRepository.findWithDetailsAndKeywordsById(shorts.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getNickname()).isEqualTo("nickname");
        assertThat(result.get().getCategory().getName()).isEqualTo("Test Category");
    }
}
