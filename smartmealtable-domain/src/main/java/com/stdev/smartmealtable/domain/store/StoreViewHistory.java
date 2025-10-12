package com.stdev.smartmealtable.domain.store;

import java.time.LocalDateTime;

/**
 * 가게 조회 이력을 담는 값 객체
 * 추천 알고리즘의 최근 관심도 계산 및 최근 7일 조회수 집계에 사용됩니다.
 */
public record StoreViewHistory(
        Long storeViewHistoryId,
        Long storeId,
        Long memberId,
        LocalDateTime viewedAt
) {
}
