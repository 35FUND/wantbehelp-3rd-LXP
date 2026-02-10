package com.example.shortudy.domain.like;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.category.repository.CategoryRepository;
import com.example.shortudy.domain.like.service.ShortsLikeService;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.upload.service.ShortsUploadInitService;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.entity.UserRole;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.config.S3Config;
import com.example.shortudy.global.config.S3Service;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("좋아요 통합 테스트")
public class ShortsLikeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortsLikeService shortsLikeService;

    @Autowired
    private ShortsRepository shortsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private CustomUserDetails customUserDetails;

    @MockitoBean
    private ShortsUploadInitService shortsUploadInitService;

    @MockitoBean
    private com.example.shortudy.global.config.S3Config s3Config;

    @MockitoBean
    private software.amazon.awssdk.services.s3.presigner.S3Presigner s3Presigner;

    @MockitoBean
    private software.amazon.awssdk.services.s3.S3Client s3Client;

    private User user;
    private Shorts shorts;

    @BeforeEach
    void setUp() {
        user = User.create("test@example.com", "password", "nickname", UserRole.USER);
        userRepository.save(user);
        customUserDetails = new CustomUserDetails(user);

        Category category = new Category("category");
        categoryRepository.save(category);

        shorts = new Shorts(user, category, "title", "description",
                "http://video.url", "http://thumbnail.url", 500, ShortsStatus.PUBLISHED);
        shortsRepository.save(shorts);
    }

    @Test
    @Transactional
    @DisplayName("좋아요 토글 통합 테스트: 실제 서비스 로직을 거쳐 상태가 변경된다")
    void toggleLikeFullCycleTest() throws Exception {
        Long targetShortsId = shorts.getId();

        // API 호출 : 좋아요 등록
        mockMvc.perform(post("/api/v1/shorts/{shortsId}/likes", targetShortsId)
                        .with(user(customUserDetails))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLiked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));

        // API 재 호출 : 좋아요 취소
        mockMvc.perform(post("/api/v1/shorts/{shortsId}/likes", targetShortsId)
                        .with(user(customUserDetails))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLiked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(0));
    }

    @Test
    @Disabled("현재 대책 세우는 중")
    @DisplayName("동시에 100명이 좋아요를 누르면 likeCount가 100이 되어야 한다")
    void shouldUpdateLikeCount100_WhenPushLikeConcurrency() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long userId = (long)(i + 1);
            executorService.submit(() -> {
                try {
                    shortsLikeService.toggleLike(userId, shorts.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Shorts foundShorts = shortsRepository.findById(shorts.getId()).orElseThrow();
        // 과연 100일까요? 아니면 40~50 정도일까요?
        assertEquals(100, foundShorts.getLikeCount());
        shortsRepository.delete(foundShorts);
    }
}
