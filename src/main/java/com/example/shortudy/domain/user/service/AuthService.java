package com.example.shortudy.domain.user.service;

import com.example.shortudy.domain.user.dto.request.LoginRequest;
import com.example.shortudy.domain.user.dto.response.LoginResponse;
import com.example.shortudy.domain.user.entity.RefreshToken;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.RefreshTokenRepository;
import com.example.shortudy.domain.user.repository.UserRepository;
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
        User user = userRepository.findByEmail(request.email()).orElseThrow(IllegalArgumentException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) throw new IllegalArgumentException("Wrong password");

        return generateToken(user);
    }

    public void logout(Long userId) {
        // 토큰이 있으면 지우고, 없으면 아무것도 안 함 (멱등성 유지)
        refreshTokenRepository.findByUserId(userId).ifPresent(refreshTokenRepository::delete);
    }

    public LoginResponse refresh(String refreshToken) {
        // 토큰 검증 로직 TODO 토큰 만료(INVALID_TOKEN)으로 처리
        if (!jwtTokenProvider.validateToken(refreshToken)) throw new IllegalArgumentException("Invalid token");

        // 토큰에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        // TODO USER_NOT_FOUND 에러로 처리
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not founded"));
        // DB에 있는 refreshToken TODO LOGIN_REQUIRED 로 처리
        RefreshToken storedToken = refreshTokenRepository.findByUserId(userId).orElseThrow(IllegalArgumentException::new);

        // RTR 검증 (토큰 탈취 의심 상황 -> DB에서 토큰 삭제 후 로그아웃 유도용 Exception)
        // TODO TOKEN_STEAL_DETECTED 에러로 처리
        if (!storedToken.getToken().equals(refreshToken)) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Invalid token");
        }

        return generateToken(user);
    }

    private LoginResponse generateToken(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                .orElse(RefreshToken.create(user.getId(), refreshToken));

        refreshTokenEntity.updateToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(accessToken, refreshTokenEntity.getToken());
    }


//    public Map<String, Object> login(LoginRequest request) {
//        User user = userRepository.findByEmail(request.email())
//                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
//
//        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
//            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
//        }
//
//        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
//        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), user.getRole());
//
//        // user 정보만 반환 (토큰은 Controller에서 쿠키로 전달)
//        UserResponse userResponse = UserResponse.from(user);
//        LoginResponse response = new LoginResponse(userResponse);
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("accessToken", accessToken);
//        result.put("refreshToken", refreshToken);
//        result.put("response", response);
//
//        return result;
//    }

//    @Override
//    public void logout(String email) {
//        // JWT는 stateless이므로 서버에서 토큰 무효화가 어려움
//        // 실제 구현 시 Redis 등을 사용하여 블랙리스트 관리 필요
//        // 현재는 클라이언트에서 토큰 삭제로 처리
//    }

//    public AuthStatusResponse getAuthStatus(String email) {
//        if (email == null) {
//            return AuthStatusResponse.notLoggedIn();
//        }
//
//        return userRepository.findByEmail(email)
//                .map(user -> AuthStatusResponse.loggedIn(
//                        user.getEmail(),
//                        user.getName(),
//                        user.getNickname()
//                ))
//                .orElse(AuthStatusResponse.notLoggedIn());
//    }



//    @Override
//    public Map<String, Object> refreshToken(TokenRefreshRequest request) {
//        String refreshToken = request.refreshToken();
//
//        // Refresh Token 유효성 검증
//        if (!jwtTokenProvider.validateToken(refreshToken)) {
//            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
//        }
//
//        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
//        User user = findUserByEmail(email);
//
//        // 새 Access Token 발급
//        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRoles());
//        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRoles());
//
//        // user 정보를 함께 반환
//        UserResponse userResponse = UserResponse.from(user);
//        LoginResponse response = new LoginResponse(userResponse);
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("accessToken", newAccessToken);
//        result.put("refreshToken", newRefreshToken);
//        result.put("response", response);
//
//        return result;
//    }

//    private User findUserByEmail(String email) {
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
//    }
}
