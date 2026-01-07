package com.example.shortudy.domain.user.service;

import com.example.shortudy.domain.user.dto.request.SignUpRequest;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//TODO exception 전부 재정비 필요
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void signup(SignUpRequest request) {

        //TODO 정책 확정 필요(email, nickname 중복에 관해)
        if (userRepository.existsByEmail(request.email())) throw new IllegalArgumentException("Email already exists");
        if (userRepository.existsByNickname(request.nickname())) throw new IllegalArgumentException("Nickname already exists");

        userRepository.save(User.create(request.email(), request.password(), request.nickname(),));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        userRepository.delete(user);
    }
}
