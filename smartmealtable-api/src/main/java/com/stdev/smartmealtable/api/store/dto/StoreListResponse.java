package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.domain.store.StoreWithDistance;

import java.math.BigDecimal;
import java.util.List;

/**
 * 가게 목록 조회 응답 DTO
 */
public record StoreListResponse(
        List<StoreItem> stores,
        int totalCount,
        int currentPage,
        int pageSize,
        int totalPages
) {
    public static StoreListResponse from(List<StoreWithDistance> stores, long totalCount, int page, int size) {
        List<StoreItem> storeItems = stores.stream()
                .map(StoreItem::from)
                .toList();
        
        int totalPages = (int) Math.ceil((double) totalCount / size);
        
        return new StoreListResponse(
                storeItems,
                (int) totalCount,
                page,
                size,
                totalPages
        );
    }
    
    /**
     * 가게 목록 아이템
     */
    public record StoreItem(
            Long storeId,
            String name,
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
            Boolean isOpen,
            String phoneNumber
    ) {
        public static StoreItem from(StoreWithDistance storeWithDistance) {
            Store store = storeWithDistance.store();
            return new StoreItem(
                    store.getStoreId(),
                    store.getName(),
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
                    null, // TODO: 영업 중 여부 계산
                    store.getPhoneNumber()
            );
        }
    }
}
