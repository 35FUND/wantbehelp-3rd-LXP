package com.example.shortudy.domain.like.repository;

import com.example.shortudy.domain.like.entity.ShortsLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortsLikeRepository extends JpaRepository<ShortsLike,Long> {





    public interface ShortsLikeCountProjection {
        Long getShortsId();
        long getCnt();
    }

    // 좋아요 개수 조회
    @Query("""
  select sl.shorts.id as shortsId, count(sl.id) as cnt
  from ShortsLike sl
  where sl.shorts.id in :shortsIds
  group by sl.shorts.id
""")
    List<ShortsLikeCountProjection> countLikesByShortsIds(@Param("shortsIds") List<Long> shortsIds);
}


