package com.example.shortudy.domain.comment.service;

import com.example.shortudy.domain.comment.dto.request.CommentRequest;
import com.example.shortudy.domain.comment.dto.response.CommentListResponse;
import com.example.shortudy.domain.comment.dto.response.CommentResponse;
import com.example.shortudy.domain.comment.dto.response.ReplyResponse;
import com.example.shortudy.domain.comment.entity.Comment;
import com.example.shortudy.domain.comment.query.CommentCountProvider;
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
import java.util.Map;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ShortsRepository shortsRepository;
    private final UserRepository userRepository;
    private final CommentCountProvider commentCountProvider;

    public CommentService(CommentRepository commentRepository, ShortsRepository shortsRepository, UserRepository userRepository, CommentCountProvider commentCountProvider) {
        this.commentRepository = commentRepository;
        this.shortsRepository = shortsRepository;
        this.userRepository = userRepository;
        this.commentCountProvider = commentCountProvider;
    }

    // 댓글 생성
    @Transactional
    public void createComment(Long userId, Long shortsId, CommentRequest request) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new BaseException(ErrorCode.USER_NOT_FOUND));

        Shorts shorts = shortsRepository.findById(shortsId).orElseThrow(() ->
                new BaseException(ErrorCode.SHORTS_NOT_FOUND));

        commentRepository.save(Comment.create(user, shorts, request.content()));
    }

    // 댓글 조회
    @Transactional(readOnly = true)
    public CommentListResponse findComments(Long shortsId, Long myIdOrNull) {

        List<Comment> comments = commentRepository.findCommentsWithUser(shortsId);
        List<Long> parentIds = comments.stream().map(Comment::getId).toList();

        Map<Long, Long> replyCountMap = commentCountProvider.replyCountMap(parentIds);

        List<CommentResponse> commentResponses = comments.stream()
                .map(c -> CommentResponse.from(
                                myIdOrNull,
                                c,
                                replyCountMap.getOrDefault(c.getId(), 0L)
                        )
                ).toList();

        // 전체 댓글 수 조회 (대댓글 포함, ACTIVE 상태만)
        long totalCount = commentRepository.countByShortsId(shortsId);

        return new CommentListResponse(totalCount, commentResponses);
    }

    // 댓글, 대댓글 수정
    @Transactional
    public void updateComment(Long userId, Long commentId, CommentRequest request) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isWrittenBy(userId)) {
            throw new BaseException(ErrorCode.COMMENT_FORBIDDEN);
        } else {
            comment.updateContent(request.content());
        }
    }

    // 댓글, 대댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long commentId) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isWrittenBy(userId)) {
            throw new BaseException(ErrorCode.COMMENT_FORBIDDEN);
        }

        // 부모 댓글 삭제 시 대댓글도 일괄 삭제
        if (comment.getParent() == null) {
            List<Comment> replies = commentRepository.findAllByParentId(commentId);
            for (Comment reply : replies) {
                reply.softDelete(reply.getUser().getId()); // 대댓글 작성자 권한으로 삭제 처리 (혹은 강제 삭제)
            }
        }

        comment.softDelete(userId);
    }

    // 대댓글 생성
    @Transactional
    public void createReply(Long userId, Long parentId, CommentRequest request) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new BaseException(ErrorCode.USER_NOT_FOUND));

        Comment parent = commentRepository.findById(parentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND));

        commentRepository.save(Comment.reply(user, parent, request.content()));
    }

    // 대댓글 조회
    @Transactional(readOnly = true)
    public List<ReplyResponse> findReplies(Long parentId, Long myIdOrNull) {

        List<Comment> replies = commentRepository.findRepliesWithUser(parentId);

        return replies.stream()
                .map(r -> ReplyResponse.from(r, myIdOrNull))
                .toList();
    }

    private CommentResponse toCommentResponse(Long meIdOrNull, Comment comment, Map<Long, Long> replyCountMap) {
        long replyCount = replyCountMap.getOrDefault(comment.getId(), 0L);
        return CommentResponse.from(meIdOrNull, comment, replyCount);
    }
}
