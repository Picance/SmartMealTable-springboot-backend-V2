package com.stdev.smartmealtable.admin.store.controller.response;

import com.stdev.smartmealtable.admin.store.service.dto.StoreServiceResponse;
import com.stdev.smartmealtable.admin.storeimage.controller.response.StoreImageResponse;
import com.stdev.smartmealtable.domain.store.StoreType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 음식점 상세 정보 Response DTO (Controller) - v2.0
 * 
 * <p>imageUrl 필드는 하위 호환성을 위해 유지하며, 새로운 images 배열을 추가했습니다.</p>
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
        String imageUrl, // 하위 호환성을 위해 유지 (대표 이미지 URL)
        List<StoreImageResponse> images, // 모든 이미지 목록
        LocalDateTime registeredAt
) {
    public static StoreResponse from(StoreServiceResponse serviceResponse) {
        // imageUrl은 대표 이미지 또는 첫 번째 이미지의 URL로 설정
        String mainImageUrl = serviceResponse.images().stream()
                .filter(StoreImageResponse::isMain)
                .map(StoreImageResponse::imageUrl)
                .findFirst()
                .or(() -> serviceResponse.images().stream()
                        .map(StoreImageResponse::imageUrl)
                        .findFirst())
                .orElse(null);
        
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
                mainImageUrl,
                serviceResponse.images(),
                serviceResponse.registeredAt()
        );
    }
}
