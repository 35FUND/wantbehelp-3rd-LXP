package com.example.shortudy.domain.user.service;

import com.example.shortudy.domain.user.dto.request.PasswordChangeRequest;
import com.example.shortudy.domain.user.dto.request.PresignedUrlResponse;
import com.example.shortudy.domain.user.dto.request.SignUpRequest;
import com.example.shortudy.domain.user.dto.request.UpdateProfileRequest;
import com.example.shortudy.domain.user.dto.response.InfoResponse;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.entity.UserRole;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.config.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    //TODO application.yaml에 빼야하나?
    private final long UPLOAD_PROFILE_MAX_FILE_SIZE = 5 * 1024 * 1024;  // 5MB 제한

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       S3Service s3Service) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3Service = s3Service;
    }

    @Transactional
    public void signup(SignUpRequest request) {

        //TODO 정책 확정 필요(email, nickname 중복에 관해)
        if (userRepository.existsByEmail(request.email())) throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
        if (userRepository.existsByNickname(request.nickname())) throw new BaseException(ErrorCode.DUPLICATE_NICKNAME);

        String encodedPassword = passwordEncoder.encode(request.password());

        userRepository.save(User.create(
                request.email(),
                encodedPassword,
                request.nickname(),
                UserRole.USER,
                request.profileUrl()
        ));
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {

        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        boolean changed = false;

        if (request.email() != null) {
            if (userRepository.existsByEmail(request.email())) throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
            user.changeEmail(request.email());
            changed = true;
        }

        if (request.profileUrl() != null) {
            user.changeProfileUrl(request.profileUrl());
            changed = true;
        }

        if (request.nickName() != null) {
            user.changeNickname(request.nickName());
            changed = true;
        }

        if (!changed) throw new BaseException(ErrorCode.INVALID_INPUT);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) throw new BaseException(ErrorCode.INVALID_PASSWORD);

        // 새 비밀번호 검증 (이전 비밀번호와 동일하지 않은지 검즘)
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) throw new BaseException(ErrorCode.SAME_PASSWORD);

        // 더티체크로 저장
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }


    // DB 상태 변화를 일으키지 않기 때문에 Transactional 사용하지 않았음
    // TODO 전체적인 flow를 분할시켰습니다. 추후 병합할 수 있는 부분 병합 예정
    // TODO 파일의 메타데이터를 FE가 보내줄 수 있다면 (Content-Type, FileSize) Content Type을 정의하는 로직 삭제 및 Max size보다 낮은 파일을 막는 검증 로직 추가 예정
    public PresignedUrlResponse prepareProfileUpload(Long userId, String uploadFileName) {

        // 1. 파일 확장자 추출
        String extension = uploadFileName.substring(uploadFileName.lastIndexOf("."));

        // 2. ContentType 결정 로직 (private 함수로 결정)
        String contentType = determineContentType(extension);

        // 3. 고유한 파일 이름 생성 (중복 방지를 위해 UUID와 타임스탬프 활용)
        String fileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + extension;

        // 4. S3 내의 경로 설정 (유지 보수를 위해 유저별 폴더를 나눔)
        String key = "profiles/" + userId + "/" + fileName;

        // 5. S3Service를 통해 임시 URL 발급, FE에게 "url에 올리고, 나중에 성공하면 이 key값을 나한테 다시 알려줘"라고 응답
        return s3Service.getPresignedUrl(key, contentType, UPLOAD_PROFILE_MAX_FILE_SIZE);
    }

    // Content-Type 고정 값 및 제한
    // TODO FE에서 메타데이터가 넘어오면 삭제할거임
    private String determineContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            //TODO gif도 필요?
            default -> throw new BaseException(ErrorCode.UnsupportedImageFormatException);
        };
    }

    // TODO [검증로직] 업로드 완료 신호가 들어왔을 때 서버에 있는 파일의 바이트(헤더)를 읽어 실제 형식을 확인
    @Transactional
    public void updateProfile(Long userId, String newImageKey) {

        // 보안 체크 (본인의 프로필이 아닌 것을 수정하려고 할 경우)
        String expectedPrefix = "profiles/" + userId + "/";

        if (!newImageKey.startsWith(expectedPrefix)) throw new BaseException(ErrorCode.AccessDeniedException);

        // 1. DB에서 해당 유저를 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 2. 지워야 할 옛날 파일 경로
        String oldImageKey = user.getProfileUrl();

        // 3. DB를 먼저 새로운 경로로 업데이트
        // [트랜잭션] 에러가 나면 아래의 삭제 로직은 실행되지 않음
        user.changeProfileUrl(newImageKey);

        // 4. 기존 사진이 있었다면 S3에서 제거
        // 파일 삭제는 실패하더라도 유저의 프로필 변경(DB) 자체가 취소되지 않게 처리
        if (oldImageKey != null && !oldImageKey.isEmpty()) {
            try {
                s3Service.deleteFile(oldImageKey);
            } catch (Exception e) {
                // 삭제 실패 시 로그를 남기고 넘김 (나중에 삭제 필요)
                //TODO 로그로 남기는게 맞음? 아니면 변경을 막는게 맞나?
                log.error("기존 프로필 파일 삭제 실패: {}", oldImageKey, e);
            }
        }
    }

    //TODO ADMIN(백오피스) 정책 의논 필요 -> FE분들이 ADMIN을 직접 DB에 넣는 작업을 하지 않기 위한 임시 서비스
    @Transactional
    public void changeAdmin(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        user.changeRole();
    }

    @Transactional(readOnly = true)
    public InfoResponse getUserInfo(Long userId) {

        return InfoResponse.from(userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND)));
    }

    //TODO User도 SoftDelete 해야할 것 같음 수정 예정
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.findById(userId).ifPresent(userRepository::delete);
    }
}
