package com.example.shortudy.domain.user.controller;

import com.example.shortudy.domain.user.dto.request.UpdateProfileRequest;
import com.example.shortudy.global.security.principal.CustomUserDetails;
import com.example.shortudy.domain.user.dto.request.SignUpRequest;
import com.example.shortudy.domain.user.dto.response.InfoResponse;
import com.example.shortudy.domain.user.service.UserService;
import com.example.shortudy.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Valid SignUpRequest request) {
        userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @RequestBody @Valid UpdateProfileRequest request) {

        userService.updateProfile(userDetails.getId(), request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<InfoResponse>> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        InfoResponse response = userService.getUserInfo(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // TODO 회원 탈퇴용, 관리자(회원 강퇴?)는 협의
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }

//    실제 프론트 분들이 어떻게 쓰시는지 논의 필요
//    @GetMapping("/{userId}")
//    public ResponseEntity<UserResponse> getUser(
//            @PathVariable Long userId) {
//        UserResponse response = userService.getUser(userId);
//        return ResponseEntity.ok(response);
//    }
}

