package com.example.shortudy.domain.user.service;

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

import java.time.LocalDateTime;

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

    @Override
    @Transactional
    public void logout(String accessToken) {

    }

    @Transactional
    public String issueRefreshToken(String email, String tokenValue) {

        // 삭제 진행
        refreshTokenRepository.deleteByUserEmail(email);

        // 삭제 쿼리 DB에 즉시 반영
        // 안 할 시 save 할 때 중복 에러 발 생
        refreshTokenRepository.flush();

        // 새로 생성 및 저장
        RefreshToken refreshToken = new RefreshToken(email, tokenValue, LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration()));
        refreshToken.setTokenValue(tokenValue);

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
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
    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest request) {
        User user = findUserByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("잘못된 비밀번호입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRoles());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRoles());

        return new UserLoginResponse(accessToken, refreshToken);
    }


    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("가입되지 않은 이메일입니다."));
    }

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
}
