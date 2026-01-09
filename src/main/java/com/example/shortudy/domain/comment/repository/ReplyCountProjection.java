package com.example.shortudy.domain.comment.repository;

public interface ReplyCountProjection {

    Long getParentId();
    long getCnt();

}
