package com.example.shortudy.domain.comment.service;

import com.example.shortudy.domain.comment.dto.request.CommentRequest;
import com.example.shortudy.domain.comment.dto.response.CommentResponse;
import com.example.shortudy.domain.comment.entity.Comment;
import com.example.shortudy.domain.comment.repository.CommentRepository;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ShortsRepository shortsRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, ShortsRepository shortsRepository,  UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.shortsRepository = shortsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentResponse createComment(Long userId, Long shortsId, CommentRequest request) {

        User user =  userRepository.findById(userId).orElseThrow(() ->
                new BaseException(ErrorCode.USER_NOT_FOUND));

        Shorts shorts = shortsRepository.findById(shortsId).orElseThrow(() ->
                new BaseException(ErrorCode.SHORTS_NOT_FOUND));

        Comment comment = Comment.create(user, shorts, request.content());

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByShortsId(Long shortsId) {

        shortsRepository.findById(shortsId).orElseThrow(() ->
                new BaseException(ErrorCode.SHORTS_NOT_FOUND));

        return commentRepository.findAllByShortsId(shortsId)
                .stream()
                .map(this::from)
                .toList();
    }

    private CommentResponse from(Comment comment) {
        return CommentResponse.from(comment);

    }

    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, CommentRequest request) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND));

        if(comment.isWrittenBy(userId)) {
            throw new BaseException(ErrorCode.COMMENT_FORBIDDEN);
        }
        comment.updateContent(request.content());

        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND));

        if(comment.isWrittenBy(userId)) {
            throw new BaseException(ErrorCode.COMMENT_FORBIDDEN);
        }
        commentRepository.deleteById(commentId);
    }
}
