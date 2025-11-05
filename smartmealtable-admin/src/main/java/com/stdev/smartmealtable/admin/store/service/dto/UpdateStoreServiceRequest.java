package com.stdev.smartmealtable.admin.store.service.dto;

import com.stdev.smartmealtable.domain.store.StoreType;

import java.math.BigDecimal;

/**
 * 음식점 수정 Service Request
 */
public record UpdateStoreServiceRequest(
        String name,
        Long categoryId,
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
    public static UpdateStoreServiceRequest of(
            String name,
            Long categoryId,
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
        return new UpdateStoreServiceRequest(
                name,
                categoryId,
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
