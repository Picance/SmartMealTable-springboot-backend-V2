package com.stdev.smartmealtable.domain.search;

/**
 * 검색 키워드 집계 결과
 */
public record SearchKeywordAggregate(
        String prefix,
        String keyword,
        long searchCount,
        long clickCount
) {
}
