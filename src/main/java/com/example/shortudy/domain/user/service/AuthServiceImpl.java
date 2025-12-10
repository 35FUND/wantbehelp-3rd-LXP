package com.example.shortudy.domain.user.service;

import com.example.shortudy.domain.user.dto.UserResponse;
import com.example.shortudy.domain.user.dto.request.UserLoginRequest;
import com.example.shortudy.domain.user.dto.request.UserSignUpRequest;
import com.example.shortudy.domain.user.dto.response.UserLoginResponse;
import com.example.shortudy.domain.user.entity.RefreshToken;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.RefreshTokenRepository;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.error.exception.EmailAlreadyExistsException;
import com.example.shortudy.global.error.exception.InvalidPasswordException;
import com.example.shortudy.global.error.exception.UserNotFoundException;
import com.example.shortudy.global.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // ------------------- 토큰 저장/갱신

    @Transactional
    public void issueRefreshToken(String email, String tokenValue, LocalDateTime expiryDate) {
        // 기존 토큰 조회 : 이메일로 기존 RefreshToken을 찾아봅니다.
        // userEmail을 PK로 사용하거나 Unique Key로 사용하는 것을 가정합니다.
        refreshTokenRepository.findByUserEmail(email)
                .ifPresentOrElse(
                        token -> {
                            token.setTokenValue(tokenValue);
                            token.setExpiryDate(expiryDate);
                        },
                        () -> {
                            // 기존 토큰이 없으면 새로 생성
                            RefreshToken newRefreshToken = new RefreshToken(email, tokenValue, expiryDate);
                            refreshTokenRepository.save(newRefreshToken);
                        }
                );
    }

    @Override
    @Transactional
    public void logout(String accessToken) {

        // Access Token의 유효성 검사 (써명검사 ). 만료는 검사하지 않음

        if (!jwtTokenProvider.validateToken(accessToken)) {
            // 변조된 토큰일 경우 무시하거나 적절한 예외 처리 (예: throw InvalidTokenException)
            throw new IllegalArgumentException("유효하지 않은 액세스 토큰입니다.");
        }

        String email = jwtTokenProvider.getEmail(accessToken);

        refreshTokenRepository.deleteByUserEmail(email);
    }

    // -------------------회원가입
    @Override
    @Transactional
    public void signup(UserSignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        User user = User.createUser(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getNickname()
        );
        userRepository.save(user);
    }

    // --------------- 로그인

    @Override
    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {


         // 사용자 인증 및 조회 (읽기 작업)
        User user = findUserByEmail(request.getEmail());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("잘못된 비밀번호입니다.");
        }

        // 토큰 발급
        String userEmail = user.getEmail(); // 이메일을 미리 추출
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRoles());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRoles());

        // Refresh Token의 만료 시간 계산 (Long -> LocalDateTime 변환)
        // * JWT Provider에서 Long 타입의 만료시점 (밀리초)을 반환해야 함을 가정
        Long expirationMillis = jwtTokenProvider.getRefreshTokenExpiration();

        //Long (밀리초) -> LocalDateTime 변환 로직
        LocalDateTime expiryDate = Instant.ofEpochMilli(expirationMillis)
                                            .atZone(ZoneId.systemDefault())
                                           .toLocalDateTime();

        // DB에 Refresh Token 저장/갱신
        issueRefreshToken(accessToken, refreshToken, expiryDate);

        // 유저 정보를 담는 DTO 생성
        UserResponse userInfo = new UserResponse(
                user.getId(), // User 엔티티에서 ID 꺼내기
                user.getEmail(), // User 엔티티에서 이메일 꺼내기
                user.getName(), // User 엔티티에서 이름 꺼내기
                user.getNickname(), // User 엔티티에서 닉네임 꺼내기
                null // 프로필 이미지 URL이 없으므로 null로 설정
        );
        // 토큰 + 유저 정보 반환
        return new UserLoginResponse(accessToken, refreshToken, userInfo);
    }


    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("가입되지 않은 이메일입니다."));
    }

}
