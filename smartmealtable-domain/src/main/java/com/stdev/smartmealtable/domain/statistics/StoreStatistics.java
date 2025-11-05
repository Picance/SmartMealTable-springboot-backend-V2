package com.stdev.smartmealtable.domain.statistics;

import java.util.Map;

/**
 * 음식점 통계 정보
 * POJO record 타입 - Spring Data 의존성 없음
 */
public record StoreStatistics(
        long totalStores,
        Map<String, Long> storesByCategory,     // categoryName -> count
        Map<String, Long> storesByType,         // CAMPUS_RESTAURANT, RESTAURANT, etc.
        long totalFoods,
        double averageFoodsPerStore,
        java.util.List<TopStore> topStoresByViews,
        java.util.List<TopStore> topStoresByReviews,
        java.util.List<TopStore> topStoresByFavorites
) {
    /**
     * 상위 음식점 정보
     */
    public record TopStore(
            Long storeId,
            String storeName,
            long count
    ) {
    }
}
