package com.example.shortudy.domain.like.Controller;

import com.example.shortudy.domain.like.dto.LikeToggleResponse;
import com.example.shortudy.domain.like.service.ShortsLikeService;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.entity.UserRole;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(ShortsLikeController.class)
class ShortsLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortsLikeService shortsLikeService;

    @Test
    @DisplayName("TC-SLC-001: 숏츠 좋아요 토글 요청 시 200 상태코드와 결과 데이터가 반환된다")
    void shouldReturnCreated_WhenToggleLike() throws Exception {
        // 1. 가짜 엔티티와 UserDetails 생성
        User mockUserEntity = mock(User.class);
        given(mockUserEntity.getId()).willReturn(1L);
        given(mockUserEntity.getEmail()).willReturn("test@test.com");
        given(mockUserEntity.getRole()).willReturn(UserRole.USER);
        CustomUserDetails customUserDetails = new CustomUserDetails(mockUserEntity);

        // 2. 서비스 응답 설정
        Long shortsId = 1L;
        LikeToggleResponse response = new LikeToggleResponse(true, 101);
        given(shortsLikeService.toggleLike(eq(1L), eq(shortsId))).willReturn(response);

        // 3. 실행 (with(user(customUserDetails)) 사용)
        mockMvc.perform(post("/api/v1/shorts/{shortsId}/likes", shortsId)
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLiked").value(true));
    }
}