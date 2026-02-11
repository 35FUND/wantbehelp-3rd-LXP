package com.example.shortudy.domain.user.service;

import com.example.shortudy.domain.user.dto.request.LoginRequest;
import com.example.shortudy.domain.user.dto.response.LoginResponse;
import com.example.shortudy.domain.user.entity.RefreshToken;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.RefreshTokenRepository;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import com.example.shortudy.global.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO 주석 세분화 필요
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) throw new BaseException(ErrorCode.INVALID_PASSWORD);

        user.updateLastLoginAt();

        return generateToken(user);
    }

    public void logout(Long userId) {
        // 토큰이 있으면 지우고, 없으면 아무것도 안 함 (멱등성 유지)
        refreshTokenRepository.findByUserId(userId).ifPresent(refreshTokenRepository::delete);
    }

    public LoginResponse refresh(String refreshToken) {
        // 토큰 검증 로직
        jwtTokenProvider.validateToken(refreshToken);

        // 토큰에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        // 유효한 세션이 아니거나 이미 로그아웃된 상태 -> 다시 로그인을 유도
        RefreshToken storedToken = refreshTokenRepository.findByUserId(userId).orElseThrow(() -> new BaseException(ErrorCode.LOGIN_REQUIRED));

        // RTR 검증 (토큰 탈취 의심 상황 -> DB에서 토큰 삭제 후 로그아웃 유도용 Exception)
        if (!storedToken.getToken().equals(refreshToken)) {
            refreshTokenRepository.delete(storedToken);
            throw new BaseException(ErrorCode.INVALID_TOKEN);
        }

        return generateToken(user);
    }

    private LoginResponse generateToken(User user) {

        // 액세스 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        // 리프레시 토큰 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        // 리프레쉬 토큰이 없으면 생성
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                .orElse(RefreshToken.create(user.getId(), refreshToken));

        // 리프레쉬 토큰이 존재하면 업데이트
        refreshTokenEntity.updateToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(accessToken, refreshTokenEntity.getToken());
    }
}
