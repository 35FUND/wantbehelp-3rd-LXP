package com.example.shortudy.domain.like.Controller;

import com.example.shortudy.domain.like.dto.LikeToggleResponse;
import com.example.shortudy.domain.like.dto.MyLikedShortsResponse;
import com.example.shortudy.domain.like.dto.SortStandard;
import com.example.shortudy.domain.like.service.ShortsLikeService;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.entity.UserRole;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(ShortsLikeController.class)
class ShortsLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortsLikeService shortsLikeService;

    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        User mockUserEntity = mock(User.class);
        given(mockUserEntity.getId()).willReturn(1L);
        given(mockUserEntity.getEmail()).willReturn("test@test.com");
        given(mockUserEntity.getRole()).willReturn(UserRole.USER);
        customUserDetails = new CustomUserDetails(mockUserEntity);
    }

    @Test
    @DisplayName("TC-SLC-001: 숏츠 좋아요 토글 요청 시 200 상태코드와 결과 데이터가 반환된다")
    void shouldReturnCreated_WhenToggleLike() throws Exception {
        // 서비스 응답 설정
        Long shortsId = 1L;
        LikeToggleResponse response = new LikeToggleResponse(true, 101);
        given(shortsLikeService.toggleLike(eq(1L), eq(shortsId))).willReturn(response);

        // 실행 (with(user(customUserDetails)) 사용)
        mockMvc.perform(post("/api/v1/shorts/{shortsId}/likes", shortsId)
                        .with(csrf())
                        .with(user(customUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLiked").value(true));
    }

    @Test
    @DisplayName("TC-SLC-002: 인기순 파라미터와 함께 목록 조회 시 200 OK를 반환한다")
    void getMyLikedShorts_Api_Success() throws Exception {
        MyLikedShortsResponse.MyLikedShorts response = createMockResponse();
        // 2. 서비스가 이 '실제 데이터' 리스트를 반환하도록 설정
        MyLikedShortsResponse pageResponse = new MyLikedShortsResponse(
                List.of(createMockResponse()),
                PageRequest.of(0, 10)
        );

        given(shortsLikeService.getMyLikedShorts(anyLong(), anyString(), any(Pageable.class)))
                .willReturn(pageResponse);

        // 3. 호출 및 검증
        mockMvc.perform(get("/api/v1/me/likes/shorts")
                        .param("sort", "POPULAR")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user(customUserDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("Success"))
                .andExpect(jsonPath("$.data.content[0].shortsId").value(28))
                .andExpect(jsonPath("$.data.content[0].title").value("게임은 재미를 설계하는 일"))
                .andExpect(jsonPath("$.data.content[0].keywords[0]").value("기획"))
                .andExpect(jsonPath("$.data.content[0].likeCount").value(50))
                // 페이징 메타데이터 검증 추가
                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageable.pageSize").value(10));
    }

    private MyLikedShortsResponse.MyLikedShorts createMockResponse() {
        return new MyLikedShortsResponse.MyLikedShorts(
                28L, "thumbUrl", "게임은 재미를 설계하는 일", "작성자", 10L,
                LocalDateTime.now(), "설명", "카테고리", List.of("기획"),
                "videoUrl", 50, 50, "userProfileUrl", 320
        );
    }
}