package com.stdev.smartmealtable.api.search.service;

import com.stdev.smartmealtable.domain.search.SearchKeywordAggregate;
import com.stdev.smartmealtable.domain.search.SearchKeywordEventRepository;
import com.stdev.smartmealtable.storage.cache.KeywordRankingCacheService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeywordAggregationServiceTest {

    @Mock
    private SearchKeywordEventRepository searchKeywordEventRepository;
    @Mock
    private KeywordRankingCacheService keywordRankingCacheService;
    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private Counter counter;

    private KeywordAggregationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new KeywordAggregationService(
                searchKeywordEventRepository,
                keywordRankingCacheService,
                meterRegistry
        );
        ReflectionTestUtils.setField(service, "windowMinutes", 5L);
        ReflectionTestUtils.setField(service, "prefixLength", 2);
        ReflectionTestUtils.setField(service, "maxKeywordsPerPrefix", 50);
        ReflectionTestUtils.setField(service, "redisTtlHours", 6L);
        when(meterRegistry.counter("keyword_aggregation_events_processed")).thenReturn(counter);
    }

    @Test
    void aggregateRecentEvents_updatesCacheAndMetrics() {
        when(searchKeywordEventRepository.aggregateBetween(any(), any(), eq(2)))
                .thenReturn(List.of(new SearchKeywordAggregate("김", "김치찌개", 10, 3)));

        service.aggregateRecentEvents();

        verify(keywordRankingCacheService).incrementScores(
                eq("김"),
                ArgumentMatchers.argThat(map -> map.containsKey("김치찌개")),
                eq(Duration.ofHours(6))
        );
        verify(keywordRankingCacheService).trimRanking("김", 50);
        verify(counter).increment(10);
    }
}
