package com.example.shortudy.domain.shorts.view.repository;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class RedisShortsViewCountRepository {

    private static final String COUNT_KEY = "shorts:view:count";
    private static final String PENDING_KEY = "shorts:view:pending";
    private static final String UNIQUE_KEY_PREFIX = "shorts:view:unique:";

    private final HashOperations<String, String, String> hashOperations;
    private final SetOperations<String, String> setOperations;
    private final ValueOperations<String, String> valueOperations;

    public RedisShortsViewCountRepository(StringRedisTemplate redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
        this.setOperations = redisTemplate.opsForSet();
        this.valueOperations = redisTemplate.opsForValue();
    }

    // 중복 조회 방지를 위한 방문자 키 등록
    public boolean markUniqueView(Long shortId, String visitorId, Duration ttl) {
        String key = UNIQUE_KEY_PREFIX + shortId + ":" + visitorId;
        Boolean created = valueOperations.setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(created);
    }

    // 조회수 카운터 증가
    public void increaseViewCount(Long shortsId) {
        hashOperations.increment(COUNT_KEY, shortsId.toString(), 1L);
        setOperations.add(PENDING_KEY, shortsId.toString());
    }

    // Redis에 누적된 조회수 조회
    public Map<Long, Long> findPendingViewCounts() {
        Set<String> ids = setOperations.members(PENDING_KEY);
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        List<String> idList = new ArrayList<>(ids);
        List<String> counts = hashOperations.multiGet(COUNT_KEY, idList);
        Map<Long, Long> result = new HashMap<>();

        for (int i = 0; i < idList.size(); i++) {
            String count = counts == null ? null : counts.get(i);
            if (count == null) {
                continue;
            }
            result.put(Long.parseLong(idList.get(i)), Long.parseLong(count));
        }
        return result;
    }

    // 누적 조회수 반영 후 Redis 데이터 정리
    public void clearPending(Set<Long> shortIds) {
        if (shortIds == null || shortIds.isEmpty()) {
            return;
        }
        String[] keys = shortIds.stream().map(String::valueOf).toArray(String[]::new);
        setOperations.remove(PENDING_KEY, (Object[]) keys);
        hashOperations.delete(COUNT_KEY, (Object[]) keys);
    }
}
