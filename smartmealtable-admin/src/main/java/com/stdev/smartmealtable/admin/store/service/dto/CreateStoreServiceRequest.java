package com.stdev.smartmealtable.admin.store.service.dto;

import com.stdev.smartmealtable.domain.store.StoreType;

import java.math.BigDecimal;

/**
 * 음식점 생성 Service Request
 */
public record CreateStoreServiceRequest(
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
        StoreType storeType,
        String imageUrl
) {
    public static CreateStoreServiceRequest of(
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
            StoreType storeType,
            String imageUrl
    ) {
        return new CreateStoreServiceRequest(
                name,
                categoryId,
                sellerId,
                address,
                lotNumberAddress,
                latitude,
                longitude,
                phoneNumber,
                description,
                averagePrice,
                storeType,
                imageUrl
        );
    }
}
