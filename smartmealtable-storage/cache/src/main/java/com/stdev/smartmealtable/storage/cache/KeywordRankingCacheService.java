package com.stdev.smartmealtable.storage.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

/**
 * 키워드 추천 Sorted Set 캐시
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordRankingCacheService {

    private static final String KEYWORD_KEY_PREFIX = "keyword:prefix:";

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 키워드 점수를 증가시키고 TTL을 갱신한다.
     *
     * @param prefix prefix (길이 1~3)
     * @param keywordScores 키워드 → 증가시킬 점수
     * @param ttl 캐시 TTL
     */
    public void incrementScores(String prefix, Map<String, Double> keywordScores, Duration ttl) {
        if (keywordScores.isEmpty()) {
            return;
        }
        ZSetOperations<String, String> ops = redisTemplate.opsForZSet();
        String key = buildKey(prefix);
        keywordScores.forEach((keyword, score) -> ops.incrementScore(key, keyword, score));
        redisTemplate.expire(key, ttl);
    }

    /**
     * Sorted Set의 크기를 제한한다.
     *
     * @param prefix prefix
     * @param maxSize 최대 유지 개수
     */
    public void trimRanking(String prefix, int maxSize) {
        String key = buildKey(prefix);
        Long size = redisTemplate.opsForZSet().zCard(key);
        if (size != null && size > maxSize) {
            long removeCount = size - maxSize;
            redisTemplate.opsForZSet().removeRange(key, 0, removeCount - 1);
            log.debug("Trimmed keyword ranking for prefix={}, removed={}", prefix, removeCount);
        }
    }

    private String buildKey(String prefix) {
        return KEYWORD_KEY_PREFIX + prefix;
    }
}
