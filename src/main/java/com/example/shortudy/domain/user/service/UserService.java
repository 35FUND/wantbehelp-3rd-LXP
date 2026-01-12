package com.example.shortudy.domain.user.service;

import com.example.shortudy.domain.user.dto.request.PasswordChangeRequest;
import com.example.shortudy.domain.user.dto.request.SignUpRequest;
import com.example.shortudy.domain.user.dto.request.UpdateProfileRequest;
import com.example.shortudy.domain.user.dto.response.InfoResponse;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.entity.UserRole;
import com.example.shortudy.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void signup(SignUpRequest request) {

        //TODO 정책 확정 필요(email, nickname 중복에 관해)
        if (userRepository.existsByEmail(request.email())) throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
        if (userRepository.existsByNickname(request.nickname())) throw new BaseException(ErrorCode.USER_NOT_FOUND);

        String encodedPassword = passwordEncoder.encode(request.password());

        // TODO 현재 ADMIN 유저는 추가할 수 없음 추후 예정
        userRepository.save(User.create(
                request.email(),
                encodedPassword,
                request.nickname(),
                UserRole.USER,
                request.profileUrl()
        ));
    }

    public void updateProfile(Long userId, UpdateProfileRequest request) {

        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        boolean changed = false;

        if (request.email() != null) {
            if (userRepository.existsByEmail(request.email())) throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
            user.changeEmail(request.email());
            changed = true;
        }

        if (request.userProfileUrl() != null) {
            user.changeProfileUrl(request.userProfileUrl());
            changed = true;
        }

        if (request.nickName() != null) {
            user.changeNickname(request.nickName());
            changed = true;
        }

        if (!changed) throw new BaseException(ErrorCode.INVALID_INPUT);
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) throw new BaseException(ErrorCode.INVALID_PASSWORD);

        // 새 비밀번호 검증 (이전 비밀번호와 동일하지 않은지 검즘)
        if (!passwordEncoder.matches(request.newPassword(), user.getPassword())) throw new BaseException(ErrorCode.SAME_PASSWORD);

        // 더티체크로 저장
        user.changePassword(request.newPassword());
    }

    public InfoResponse getUserInfo(Long userId) {

        return InfoResponse.from(userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND)));
    }

    public void deleteUser(Long userId) {
        userRepository.findById(userId).ifPresent(userRepository::delete);
    }
}
