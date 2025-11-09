package com.stdev.smartmealtable.admin.store.service.dto;

import com.stdev.smartmealtable.admin.storeimage.controller.response.StoreImageResponse;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreImage;
import com.stdev.smartmealtable.domain.store.StoreType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 음식점 상세 정보 Service Response - v2.0
 */
public record StoreServiceResponse(
        Long storeId,
        String name,
        List<Long> categoryIds,
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
        String imageUrl, // 하위 호환성을 위해 유지
        List<StoreImageResponse> images, // 모든 이미지 목록
        LocalDateTime registeredAt,
        LocalDateTime deletedAt
) {
    public static StoreServiceResponse from(Store store) {
        return new StoreServiceResponse(
                store.getStoreId(),
                store.getName(),
                store.getCategoryIds(),
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
                null, // imageUrl은 나중에 설정
                Collections.emptyList(), // images는 나중에 설정
                store.getRegisteredAt(),
                store.getDeletedAt()
        );
    }
    
    /**
     * Store와 이미지 목록으로부터 생성
     */
    public static StoreServiceResponse from(Store store, List<StoreImage> images) {
        List<StoreImageResponse> imageResponses = images.stream()
                .map(StoreImageResponse::from)
                .toList();
        
        // imageUrl은 대표 이미지 또는 첫 번째 이미지의 URL로 설정
        String mainImageUrl = imageResponses.stream()
                .filter(StoreImageResponse::isMain)
                .map(StoreImageResponse::imageUrl)
                .findFirst()
                .or(() -> imageResponses.stream()
                        .map(StoreImageResponse::imageUrl)
                        .findFirst())
                .orElse(null);
        
        return new StoreServiceResponse(
                store.getStoreId(),
                store.getName(),
                store.getCategoryIds(),
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
                mainImageUrl,
                imageResponses,
                store.getRegisteredAt(),
                store.getDeletedAt()
        );
    }
}
