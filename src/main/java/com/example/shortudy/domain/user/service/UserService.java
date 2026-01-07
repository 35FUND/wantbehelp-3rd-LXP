package com.example.shortudy.domain.user.service;

import com.example.shortudy.domain.user.dto.request.SignUpRequest;
import com.example.shortudy.domain.user.dto.response.InfoResponse;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.entity.UserRole;
import com.example.shortudy.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//TODO exception 전부 재정비 필요
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
        if (userRepository.existsByEmail(request.email())) throw new IllegalArgumentException("Email already exists");
        if (userRepository.existsByNickname(request.nickname())) throw new IllegalArgumentException("Nickname already exists");

        String encodedPassword = passwordEncoder.encode(request.password());

        userRepository.save(User.create(
                request.email(),
                encodedPassword,
                request.nickname(),
                UserRole.USER,
                request.profileUrl()
        ));
    }

    public InfoResponse getUserInfo(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        //TODO InfoResponse의 from 메서드 작성
        return new InfoResponse(user.getId(), user.getEmail(), user.getNickname(), user.getProfileUrl());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }
}
