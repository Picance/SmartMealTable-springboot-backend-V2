package com.stdev.smartmealtable.admin.statistics.dto.response;

import com.stdev.smartmealtable.domain.statistics.StoreStatistics;

import java.util.List;
import java.util.Map;

/**
 * 음식점 통계 응답 DTO
 */
public record StoreStatisticsResponse(
        long totalStores,
        Map<String, Long> storesByCategory,
        Map<String, Long> storesByType,
        long totalFoods,
        double averageFoodsPerStore,
        List<TopStoreResponse> topStoresByViews,
        List<TopStoreResponse> topStoresByReviews,
        List<TopStoreResponse> topStoresByFavorites
) {
    /**
     * 상위 음식점 응답 DTO
     */
    public record TopStoreResponse(
            Long storeId,
            String storeName,
            long count
    ) {
        public static TopStoreResponse from(StoreStatistics.TopStore topStore) {
            return new TopStoreResponse(
                    topStore.storeId(),
                    topStore.storeName(),
                    topStore.count()
            );
        }
    }

    public static StoreStatisticsResponse from(StoreStatistics statistics) {
        return new StoreStatisticsResponse(
                statistics.totalStores(),
                statistics.storesByCategory(),
                statistics.storesByType(),
                statistics.totalFoods(),
                statistics.averageFoodsPerStore(),
                statistics.topStoresByViews().stream()
                        .map(TopStoreResponse::from)
                        .toList(),
                statistics.topStoresByReviews().stream()
                        .map(TopStoreResponse::from)
                        .toList(),
                statistics.topStoresByFavorites().stream()
                        .map(TopStoreResponse::from)
                        .toList()
        );
    }
}
