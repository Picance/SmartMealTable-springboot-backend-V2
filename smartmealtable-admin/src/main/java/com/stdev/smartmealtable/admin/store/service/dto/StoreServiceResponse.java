package com.stdev.smartmealtable.admin.store.service.dto;

import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 음식점 상세 정보 Service Response
 */
public record StoreServiceResponse(
        Long storeId,
        String name,
        Long categoryId,
        Long sellerId,
        String address,
        String lotNumberAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        String phoneNumber,
        String description,
        Integer averagePrice,
        Integer reviewCount,
        Integer viewCount,
        Integer favoriteCount,
        StoreType storeType,
        String imageUrl,
        LocalDateTime registeredAt,
        LocalDateTime deletedAt
) {
    public static StoreServiceResponse from(Store store) {
        return new StoreServiceResponse(
                store.getStoreId(),
                store.getName(),
                store.getCategoryId(),
                store.getSellerId(),
                store.getAddress(),
                store.getLotNumberAddress(),
                store.getLatitude(),
                store.getLongitude(),
                store.getPhoneNumber(),
                store.getDescription(),
                store.getAveragePrice(),
                store.getReviewCount(),
                store.getViewCount(),
                store.getFavoriteCount(),
                store.getStoreType(),
                store.getImageUrl(),
                store.getRegisteredAt(),
                store.getDeletedAt()
        );
    }
}
