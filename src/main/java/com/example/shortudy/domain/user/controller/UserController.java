package com.example.shortudy.domain.user.controller;

import com.example.shortudy.domain.user.dto.request.PasswordChangeRequest;
import com.example.shortudy.domain.user.dto.request.PresignedUrlResponse;
import com.example.shortudy.domain.user.dto.request.ProfileImageUpdateRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Valid SignUpRequest request) {
        userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<InfoResponse>> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        InfoResponse response = userService.getUserInfo(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/profile/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String fileName) {
        return ResponseEntity.ok(ApiResponse.success(userService.prepareProfileUpload(userDetails.getId(), fileName)));
    }

    @PatchMapping("/me/profile/image")
    public ResponseEntity<ApiResponse<Void>> updateProfileImage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                @RequestBody @Valid ProfileImageUpdateRequest request) {
        userService.completeProfileUpdate(userDetails.getId(), request.newImageKey());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // TODO 프로필 수정 변경 예정 (프로필 사진 변경, 닉네임 변경 분리 예정)
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @RequestBody @Valid UpdateProfileRequest request) {

        userService.updateProfile(userDetails.getId(), request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(userDetails.getId(), request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> changeRole(@PathVariable("userId") Long userId) {

        userService.changeAdmin(userId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // TODO 회원 탈퇴용, 관리자(회원 강퇴?)는 협의
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }
}

