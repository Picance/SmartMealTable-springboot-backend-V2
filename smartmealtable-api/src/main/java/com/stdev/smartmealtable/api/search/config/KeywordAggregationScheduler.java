package com.stdev.smartmealtable.api.search.config;

import com.stdev.smartmealtable.api.search.service.KeywordAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 검색 키워드 집계 스케줄러
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class KeywordAggregationScheduler {

    private final KeywordAggregationService keywordAggregationService;

    @Scheduled(fixedDelayString = "${keyword.aggregation.fixed-delay-ms:300000}")
    public void aggregate() {
        try {
            keywordAggregationService.aggregateRecentEvents();
        } catch (Exception e) {
            log.error("Keyword aggregation job failed", e);
        }
    }
}
