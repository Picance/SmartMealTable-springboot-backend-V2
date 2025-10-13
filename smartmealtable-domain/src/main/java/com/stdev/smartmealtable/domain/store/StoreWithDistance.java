package com.stdev.smartmealtable.domain.store;

import java.math.BigDecimal;

/**
 * 거리 정보를 포함한 가게 정보 DTO
 * 위치 기반 검색 결과에서 사용됩니다.
 */
public record StoreWithDistance(
        Store store,
        BigDecimal distance
) {
    public static StoreWithDistance of(Store store, BigDecimal distance) {
        return new StoreWithDistance(store, distance);
    }
    
    public static StoreWithDistance of(Store store, Double distance) {
        return new StoreWithDistance(store, distance != null ? BigDecimal.valueOf(distance) : null);
    }
}
