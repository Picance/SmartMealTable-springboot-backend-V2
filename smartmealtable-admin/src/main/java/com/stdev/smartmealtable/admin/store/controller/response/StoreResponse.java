package com.stdev.smartmealtable.admin.store.controller.response;

import com.stdev.smartmealtable.admin.store.service.dto.StoreServiceResponse;
import com.stdev.smartmealtable.domain.store.StoreType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 음식점 상세 정보 Response DTO (Controller)
 */
public record StoreResponse(
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
        LocalDateTime registeredAt
) {
    public static StoreResponse from(StoreServiceResponse serviceResponse) {
        return new StoreResponse(
                serviceResponse.storeId(),
                serviceResponse.name(),
                serviceResponse.categoryId(),
                serviceResponse.sellerId(),
                serviceResponse.address(),
                serviceResponse.lotNumberAddress(),
                serviceResponse.latitude(),
                serviceResponse.longitude(),
                serviceResponse.phoneNumber(),
                serviceResponse.description(),
                serviceResponse.averagePrice(),
                serviceResponse.reviewCount(),
                serviceResponse.viewCount(),
                serviceResponse.favoriteCount(),
                serviceResponse.storeType(),
                serviceResponse.imageUrl(),
                serviceResponse.registeredAt()
        );
    }
}
