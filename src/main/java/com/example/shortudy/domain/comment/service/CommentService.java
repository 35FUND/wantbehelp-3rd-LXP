package com.example.shortudy.domain.comment.service;

import com.example.shortudy.domain.comment.dto.request.CommentRequest;
import com.example.shortudy.domain.comment.dto.response.CommentListResponse;
import com.example.shortudy.domain.comment.dto.response.CommentResponse;
import com.example.shortudy.domain.comment.dto.response.ReplyResponse;
import com.example.shortudy.domain.comment.dto.response.WriterResponse;
import com.example.shortudy.domain.comment.entity.Comment;
import com.example.shortudy.domain.comment.entity.CommentReport;
import com.example.shortudy.domain.comment.entity.CommentStatus;
import com.example.shortudy.domain.comment.query.CommentCountProvider;
import com.example.shortudy.domain.comment.repository.CommentReportRepository;
import com.example.shortudy.domain.comment.repository.CommentRepository;
import com.example.shortudy.domain.shorts.entity.Shorts;
import com.example.shortudy.domain.shorts.repository.ShortsRepository;
import com.example.shortudy.domain.user.entity.User;
import com.example.shortudy.domain.user.repository.UserRepository;
import com.example.shortudy.global.config.S3Service;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentReportRepository commentReportRepository;
    private final ShortsRepository shortsRepository;
    private final UserRepository userRepository;
    private final CommentCountProvider commentCountProvider;
    private final S3Service s3Service;

    public CommentService(CommentRepository commentRepository, CommentReportRepository commentReportRepository,ShortsRepository shortsRepository, UserRepository userRepository, CommentCountProvider commentCountProvider, S3Service s3Service) {
        this.commentRepository = commentRepository;
        this.commentReportRepository = commentReportRepository;
        this.shortsRepository = shortsRepository;
        this.userRepository = userRepository;
        this.commentCountProvider = commentCountProvider;
        this.s3Service = s3Service;
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

        // ✅ 내가 신고한 댓글 id Set (로그인 안 했으면 empty)
        Set<Long> reportedIds = (myIdOrNull == null || parentIds.isEmpty())
            ? Set.of()
            : new HashSet<>(commentReportRepository.findReportedCommentIds(myIdOrNull, parentIds));


        // TODO : 여기서 getFileUrl() 호출하는 부분 최적화 필요
        List<CommentResponse> commentResponses = comments.stream()
                .map(c -> convertProfileUrl(CommentResponse.from(
                                myIdOrNull,
                                c,
                                replyCountMap.getOrDefault(c.getId(), 0L),
                                reportedIds.contains(c.getId())
                        ))
                ).toList();

        // 전체 댓글 수 조회 (대댓글 포함, ACTIVE 상태만)
        long totalCount = commentRepository.countByShortsId(shortsId);

        return new CommentListResponse(totalCount, commentResponses);
    }

    // 댓글 수정
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

    // NOTE : 대댓글 업데이트 로직 분리를 위한 메서드 추가
    @Transactional
    public void updateCommentReply(Long userId, Long commentId, CommentRequest request) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
            new BaseException(ErrorCode.COMMENT_NOT_FOUND));


        // 대댓글이 아닌 경우 예외 처리
        if (comment.getParent() == null) {
            throw new BaseException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 댓글이 삭제되어있는 경우 예외 처리
        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new BaseException(ErrorCode.COMMENT_DELETED);
        }

        // 작성자 검증
        if (!comment.isWrittenBy(userId)) {
            throw new BaseException(ErrorCode.COMMENT_FORBIDDEN);
        } else {
            comment.updateContent(request.content());
        }
    }

    // 댓글 삭제
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

    // TODO : 대댓글 삭제 메서드 분리
    @Transactional
    public void deleteCommentReply(Long userId, Long commentId) {

        // 대댓글 ID가 없으면 예외 처리
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
            new BaseException(ErrorCode.COMMENT_NOT_FOUND));

        // 내가 쓴 대댓글이 아니면 오류 처리
        if (!comment.isWrittenBy(userId)) {
            throw new BaseException(ErrorCode.COMMENT_FORBIDDEN);
        }

        // 대댓글이 아닌 경우 예외 처리
        if (comment.getParent() == null) {
            throw new BaseException(ErrorCode.COMMENT_NOT_FOUND);
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

        Comment parentComment = commentRepository.findById(parentId).orElseThrow(() ->
            new BaseException(ErrorCode.COMMENT_NOT_FOUND));
        if (parentComment.getParent() != null) {
            throw new BaseException(ErrorCode.COMMENT_NOT_FOUND);
        }

        List<Comment> replies = commentRepository.findRepliesWithUser(parentId);
        List<Long> replyIds = replies.stream().map(Comment::getId).toList();

        // ✅ 내가 신고한 대댓글 id Set
        Set<Long> reportedReplyIds = (myIdOrNull == null || replyIds.isEmpty())
            ? Set.of()
            : new HashSet<>(commentReportRepository.findReportedCommentIds(myIdOrNull, replyIds));

        return replies.stream()
            .map(r -> convertReplyUrl(ReplyResponse.from(
                r,
                myIdOrNull,
                reportedReplyIds.contains(r.getId())
            )))
            .toList();
    }

    // 댓글 / 대댓글 신고 (comment_id를 받기 때문에 구분하진 않음 !)
    @Transactional
    public void reportComment(Long userId, Long commentId) {

        // 신고할 댓글이 존재하는지 확인
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND));

        // 내가 해당 댓글을 신고했는지 확인 (중복 신고 방지)
        boolean alreadyReported = commentReportRepository.existsByCommentIdAndReporterId(commentId, userId);
        if (alreadyReported) {
            throw new BaseException(ErrorCode.COMMENT_ALREADY_REPORTED);
        }

        // Comment Report 엔티티 생성
        String reason = "Inappropriate content"; // TODO: 실제로는 신고 사유를 받아와야 함
        CommentReport commentReport = comment.reportByUser(userId, reason);

        // 신고 저장
        commentReportRepository.save(commentReport);
    }

//    private CommentResponse toCommentResponse(Long meIdOrNull, Comment comment, Map<Long, Long> replyCountMap) {
//        long replyCount = replyCountMap.getOrDefault(comment.getId(), 0L);
//        return CommentResponse.from(meIdOrNull, comment, replyCount);
//    }

    private CommentResponse convertProfileUrl(CommentResponse response) {
        String convertedProfileUrl = s3Service.getFileUrl(response.writer().profileImageUrl());
        return new CommentResponse(
                response.shortsId(),
                response.commentId(),
                response.content(),
                response.createdAt(),
                new WriterResponse(
                        response.writer().userId(),
                        response.writer().nickname(),
                        convertedProfileUrl
                ),
                response.replyCount(),
                response.isMine(),
                response.isReported()
        );
    }

    private ReplyResponse convertReplyUrl(ReplyResponse response) {
        String convertedProfileUrl = s3Service.getFileUrl(response.writer().profileImageUrl());
        return new ReplyResponse(
                response.replyId(),
                response.parentId(),
                response.content(),
                response.createdAt(),
                new WriterResponse(
                        response.writer().userId(),
                        response.writer().nickname(),
                        convertedProfileUrl
                ),
                response.isMine(),
                response.isReported()
        );
    }
}
