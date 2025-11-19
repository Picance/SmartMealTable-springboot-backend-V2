package com.stdev.smartmealtable.storage.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeywordRankingCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private KeywordRankingCacheService keywordRankingCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        keywordRankingCacheService = new KeywordRankingCacheService(redisTemplate);
    }

    @Test
    void getTopKeywords_returnsOrderedList() {
        LinkedHashSet<String> rankings = new LinkedHashSet<>();
        rankings.add("김치찌개");
        rankings.add("김치볶음밥");
        when(zSetOperations.reverseRange("keyword:prefix:김", 0, 1)).thenReturn(rankings);

        List<String> result = keywordRankingCacheService.getTopKeywords("김", 2);

        assertThat(result).containsExactly("김치찌개", "김치볶음밥");
    }

    @Test
    void getTopKeywords_handlesEmptyResponse() {
        when(zSetOperations.reverseRange("keyword:prefix:비", 0, 4)).thenReturn(null);

        List<String> result = keywordRankingCacheService.getTopKeywords("비", 5);

        assertThat(result).isEmpty();
    }

    @Test
    void incrementScores_incrementsEachKeywordAndSetsTtl() {
        keywordRankingCacheService.incrementScores("떡", Map.of(
                "떡볶이", 1.5,
                "로제떡볶이", 2.0
        ), Duration.ofHours(6));

        verify(zSetOperations, times(1)).incrementScore("keyword:prefix:떡", "떡볶이", 1.5);
        verify(zSetOperations, times(1)).incrementScore("keyword:prefix:떡", "로제떡볶이", 2.0);
        verify(redisTemplate, times(1)).expire(eq("keyword:prefix:떡"), eq(Duration.ofHours(6)));
    }
}
