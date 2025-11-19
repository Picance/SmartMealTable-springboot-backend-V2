package com.stdev.smartmealtable.api.search.service;

import com.stdev.smartmealtable.domain.search.SearchKeywordAggregate;
import com.stdev.smartmealtable.domain.search.SearchKeywordEventRepository;
import com.stdev.smartmealtable.storage.cache.KeywordRankingCacheService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 검색 키워드 집계 및 Redis 반영 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordAggregationService {

    private final SearchKeywordEventRepository searchKeywordEventRepository;
    private final KeywordRankingCacheService keywordRankingCacheService;
    private final MeterRegistry meterRegistry;

    @Value("${keyword.aggregation.prefix-length:2}")
    private int prefixLength;

    @Value("${keyword.aggregation.window-minutes:5}")
    private long windowMinutes;

    @Value("${keyword.aggregation.score.search-weight:0.7}")
    private double searchWeight;

    @Value("${keyword.aggregation.score.click-weight:1.3}")
    private double clickWeight;

    @Value("${keyword.aggregation.max-keywords-per-prefix:200}")
    private int maxKeywordsPerPrefix;

    @Value("${keyword.aggregation.redis-ttl-hours:12}")
    private long redisTtlHours;

    private final AtomicReference<LocalDateTime> lastProcessedAt = new AtomicReference<>();

    private Counter aggregatedCounter;

    /**
     * 최근 window 범위의 이벤트를 집계하여 Redis Sorted Set에 반영
     */
    public void aggregateRecentEvents() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime from = lastProcessedAt.updateAndGet(previous -> {
            if (previous == null) {
                return now.minusMinutes(windowMinutes);
            }
            return previous;
        });
        LocalDateTime to = now.truncatedTo(ChronoUnit.MINUTES);
        if (!to.isAfter(from)) {
            to = from.plusMinutes(windowMinutes);
        }

        List<SearchKeywordAggregate> aggregates =
                searchKeywordEventRepository.aggregateBetween(from, to, prefixLength);

        log.info("Keyword aggregation window from {} to {} produced {} rows", from, to, aggregates.size());

        if (aggregates.isEmpty()) {
            lastProcessedAt.set(to);
            return;
        }

        Map<String, Map<String, Double>> prefixMap = new HashMap<>();
        long totalSearches = 0L;

        for (SearchKeywordAggregate aggregate : aggregates) {
            String prefix = normalizePrefix(aggregate.prefix());
            if (prefix.isBlank()) {
                continue;
            }
            double score = aggregate.searchCount() * searchWeight + aggregate.clickCount() * clickWeight;
            prefixMap.computeIfAbsent(prefix, key -> new HashMap<>())
                    .merge(aggregate.keyword(), score, Double::sum);
            totalSearches += aggregate.searchCount();
        }

        prefixMap.forEach((prefix, keywords) -> {
            keywordRankingCacheService.incrementScores(prefix, keywords, Duration.ofHours(redisTtlHours));
            keywordRankingCacheService.trimRanking(prefix, maxKeywordsPerPrefix);
        });

        if (aggregatedCounter == null) {
            aggregatedCounter = meterRegistry.counter("keyword_aggregation_events_processed");
        }
        aggregatedCounter.increment(totalSearches);

        lastProcessedAt.set(to);
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null) {
            return "";
        }
        return prefix.trim();
    }
}
