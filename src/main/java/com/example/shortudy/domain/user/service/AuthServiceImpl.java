package com.example.shortudy.domain.user.service;

import com.example.shortudy.domain.user.dto.request.TokenRefreshRequest;
import com.example.shortudy.domain.user.dto.request.UserLoginRequest;
import com.example.shortudy.domain.user.dto.request.UserSignUpRequest;
import com.example.shortudy.domain.user.dto.response.AuthStatusResponse;
import com.example.shortudy.domain.user.dto.response.UserLoginResponse;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.error.exception.EmailAlreadyExistsException;
import com.example.shortudy.global.error.exception.InvalidPasswordException;
import com.example.shortudy.global.error.exception.UserNotFoundException;
import com.example.shortudy.global.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public void signup(UserSignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        User user = User.createUser(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName()
        );
        userRepository.save(user);
    }

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        User user = findUserByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException("잘못된 비밀번호입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRoles());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRoles());

        return new UserLoginResponse(accessToken, refreshToken);
    }

    @Override
    public void logout(String email) {
        // JWT는 stateless이므로 서버에서 토큰 무효화가 어려움
        // 실제 구현 시 Redis 등을 사용하여 블랙리스트 관리 필요
        // 현재는 클라이언트에서 토큰 삭제로 처리
    }

    @Override
    public AuthStatusResponse getAuthStatus(String email) {
        if (email == null) {
            return AuthStatusResponse.notLoggedIn();
        }

        return userRepository.findByEmail(email)
                .map(user -> AuthStatusResponse.loggedIn(
                        user.getEmail(),
                        user.getName(),
                        user.getNickname()
                ))
                .orElse(AuthStatusResponse.notLoggedIn());
    }

    @Override
    public UserLoginResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();

        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = findUserByEmail(email);

        // 새 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRoles());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRoles());

        return new UserLoginResponse(newAccessToken, newRefreshToken);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("가입되지 않은 이메일입니다."));
    }
}
