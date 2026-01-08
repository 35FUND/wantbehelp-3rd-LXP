package com.example.shortudy.domain.comment.service;

import com.example.shortudy.domain.comment.dto.request.CommentRequest;
import com.example.shortudy.domain.comment.dto.response.CommentResponse;
import com.example.shortudy.domain.comment.dto.response.ReplyResponse;
import com.example.shortudy.domain.comment.entity.Comment;
import com.example.shortudy.domain.comment.repository.CommentRepository;
import com.example.shortudy.domain.comment.repository.ReplyCountProjection;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ShortsRepository shortsRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, ShortsRepository shortsRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.shortsRepository = shortsRepository;
        this.userRepository = userRepository;
    }

    // 댓글 생성
    @Transactional
    public CommentResponse createComment(Long userId, Long shortsId, CommentRequest request) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new BaseException(ErrorCode.USER_NOT_FOUND));

        Shorts shorts = shortsRepository.findById(shortsId).orElseThrow(() ->
                new BaseException(ErrorCode.SHORTS_NOT_FOUND));

        Comment saved = commentRepository.save(Comment.create(user, shorts, request.content()));

        Map<Long, Long> replyCountMap = getReplyCountMap(List.of(saved.getId()));

        return toCommentResponse(saved, user.getId(), replyCountMap);
    }

    // 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> findComments(Long shortsId, Long myIdOrNull) {

        List<Comment> comments = commentRepository.findCommentsWithUser(shortsId);
        List<Long> parentIds = comments.stream().map(Comment::getId).toList();

        Map<Long, Long> replyCountMap = getReplyCountMap(parentIds);

        return comments.stream()
                .map(c -> CommentResponse.from(
                                replyCountMap.getOrDefault(c.getId(), 0L),
                                c,
                                myIdOrNull
                        )
                ).toList();
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, CommentRequest request) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND));

        // TODO : isComment(parentId == null) 추가
        if (!comment.isWrittenBy(userId)) {
            throw new BaseException(ErrorCode.COMMENT_FORBIDDEN);
        }
        comment.updateContent(request.content());

        Map<Long, Long> replyCountMap = getReplyCountMap(List.of(comment.getId()));
        return toCommentResponse(comment, userId, replyCountMap);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long commentId) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isWrittenBy(userId)) {
            throw new BaseException(ErrorCode.COMMENT_FORBIDDEN);
        }
        commentRepository.deleteById(commentId);
    }

    // 대댓글 생성
    @Transactional
}
