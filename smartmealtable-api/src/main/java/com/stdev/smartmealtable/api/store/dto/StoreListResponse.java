package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.domain.store.StoreWithDistance;

import java.math.BigDecimal;
import java.util.List;

/**
 * 가게 목록 조회 응답 DTO
 * 
 * <p>커서 기반 페이징과 오프셋 기반 페이징 모두 지원합니다.</p>
 */
public record StoreListResponse(
        List<StoreItem> stores,
        int totalCount,
        int currentPage,
        int pageSize,
        int totalPages,
        boolean hasMore,
        Long lastId
) {
    /**
     * 오프셋 기반 페이징 응답 생성 (기존 방식)
     */
    public static StoreListResponse from(List<StoreWithDistance> stores, long totalCount, int page, int size) {
        List<StoreItem> storeItems = stores.stream()
                .map(StoreItem::from)
                .toList();
        
        int totalPages = (int) Math.ceil((double) totalCount / size);
        boolean hasMore = page < totalPages - 1;
        Long lastId = !storeItems.isEmpty() ? storeItems.get(storeItems.size() - 1).storeId : null;
        
        return new StoreListResponse(
                storeItems,
                (int) totalCount,
                page,
                size,
                totalPages,
                hasMore,
                lastId
        );
    }

    /**
     * 커서 기반 페이징 응답 생성
     */
    public static StoreListResponse ofCursor(List<StoreWithDistance> stores, long totalCount, int limit, int pageSize) {
        List<StoreItem> storeItems = stores.stream()
                .map(StoreItem::from)
                .toList();
        
        boolean hasMore = storeItems.size() >= limit;
        Long lastId = !storeItems.isEmpty() ? storeItems.get(storeItems.size() - 1).storeId : null;
        
        return new StoreListResponse(
                storeItems,
                (int) totalCount,
                0,
                pageSize,
                1,
                hasMore,
                lastId
        );
    }
    
    /**
     * 가게 목록 아이템
     */
    public record StoreItem(
            Long storeId,
            String name,
            Long categoryId,
            String categoryName,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            Integer averagePrice,
            Integer reviewCount,
            Integer viewCount,
            StoreType storeType,
            String imageUrl,
            BigDecimal distance,
            String phoneNumber
    ) {
        public static StoreItem from(StoreWithDistance storeWithDistance) {
            Store store = storeWithDistance.store();
            Long primaryCategoryId = store.getCategoryIds().isEmpty() ? null : store.getCategoryIds().get(0);
            return new StoreItem(
                    store.getStoreId(),
                    store.getName(),
                    primaryCategoryId,
                    null, // TODO: Category 조인 필요
                    store.getAddress(),
                    store.getLatitude(),
                    store.getLongitude(),
                    store.getAveragePrice(),
                    store.getReviewCount(),
                    store.getViewCount(),
                    store.getStoreType(),
                    store.getImageUrl(),
                    storeWithDistance.distance(),
                    store.getPhoneNumber()
            );
        }
    }
}
