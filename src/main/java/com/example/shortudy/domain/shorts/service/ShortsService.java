package com.example.shortudy.domain.shorts.service;

import com.example.shortudy.domain.category.entity.Category;
import com.example.shortudy.domain.category.repository.CategoryRepository;
import com.example.shortudy.domain.shorts.dto.ShortsResponse;
import com.example.shortudy.domain.shorts.dto.ShortsUpdateRequest;
import com.example.shortudy.domain.shorts.dto.ShortsUploadRequest;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.entity.ShortsStatus;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.tag.entity.Tag;
import com.example.shortudy.domain.tag.service.TagService;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ShortsService {

    private final ShortsRepository shortsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagService tagService;

    public ShortsService(ShortsRepository shortsRepository, UserRepository userRepository,
                         CategoryRepository categoryRepository, TagService tagService) {
        this.shortsRepository = shortsRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagService = tagService;
    }

    @Transactional
    public ShortsResponse uploadShorts(ShortsUploadRequest shortsUploadRequest) {

        User user = userRepository.findById(shortsUploadRequest.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Category category = categoryRepository.findById(shortsUploadRequest.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        Shorts shorts = new Shorts(
                user,
                category,
                shortsUploadRequest.title(),
                shortsUploadRequest.description(),
                shortsUploadRequest.videoUrl(),
                shortsUploadRequest.thumbnailUrl(),
                shortsUploadRequest.durationSec(),
                ShortsStatus.PUBLISHED
        );

        // 태그 추가
        if (shortsUploadRequest.tagNames() != null && !shortsUploadRequest.tagNames().isEmpty()) {
            for (String tagName : shortsUploadRequest.tagNames()) {
                Tag tag = tagService.getOrCreateTag(tagName);
                if (tag != null) {
                    shorts.addTag(tag);
                }
            }
        }

        Shorts savedShorts = shortsRepository.save(shorts);
        return ShortsResponse.from(savedShorts);
    }

    @Transactional(readOnly = true)
    public ShortsResponse getShortsDetails(Long shortsId) {
        Shorts shorts = shortsRepository.findWithDetailsById(shortsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 숏츠입니다."));

        return ShortsResponse.from(shorts);
    }

    @Transactional(readOnly = true)
    public Page<ShortsResponse> getShortsList(Pageable pageable) {

        return shortsRepository.findAll(pageable).map(ShortsResponse::from);
    }

    @Transactional
    public ShortsResponse updateShorts(Long shortsId, ShortsUpdateRequest request){

        Shorts shorts = shortsRepository.findById(shortsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 숏츠입니다."));

        if(request.durationSec() != null && request.durationSec() <= 0){
            throw new IllegalArgumentException("영상 길이는 1초 이상이어야 합니다.");
        }

        Category category = null;
        if(request.categoryId() != null){
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
        }
        shorts.updateShorts(
                request.title(),
                request.description(),
                request.thumbnailUrl(),
                category,
                request.status()
        );

        // 태그 업데이트 (기존 태그 삭제 후 새로 추가)
        if (request.tagNames() != null) {
            shorts.clearTags();
            for (String tagName : request.tagNames()) {
                Tag tag = tagService.getOrCreateTag(tagName);
                if (tag != null) {
                    shorts.addTag(tag);
                }
            }
        }

        return ShortsResponse.from(shorts);
    }

    @Transactional
    public void deleteShorts(Long shortsId){

        if(!shortsRepository.existsById(shortsId)){
            throw new NoSuchElementException("존재하지 않는 숏츠입니다.");
        }

        shortsRepository.deleteById(shortsId);
    }
}
