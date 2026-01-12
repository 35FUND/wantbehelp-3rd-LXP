package com.example.shortudy.domain.comment.query;

import com.example.shortudy.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommentCountProvider {

    private final CommentRepository commentRepository;

    public CommentCountProvider(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Map<Long, Long> replyCountMap(List<Long> parentIds) {
        if (parentIds.isEmpty()) return Map.of();

        return commentRepository.countRepliesByParentIds(parentIds).stream()
                .collect(Collectors.toMap(
                        CommentRepository.ReplyCountProjection::getParentId,
                        CommentRepository.ReplyCountProjection::getCnt
                ));
    }

    public Map<Long, Long> commentCountByShortsIds(List<Long> shortsIds) {
        if (shortsIds.isEmpty()) return Map.of();

        return commentRepository.countAllCommentsByShortsIds(shortsIds).stream()
                .collect(Collectors.toMap(
                        CommentRepository.ShortsCommentCountProjection::getShortsId,
                        CommentRepository.ShortsCommentCountProjection::getCnt
                ));
    }
}
