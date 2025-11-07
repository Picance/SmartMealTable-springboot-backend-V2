package com.stdev.smartmealtable.admin.store.service.dto;

import com.stdev.smartmealtable.domain.store.StoreType;

/**
 * 음식점 수정 Service Request - v2.0
 * 
 * <p>latitude, longitude, imageUrl 필드 제거됨</p>
 * <p>latitude, longitude는 서버에서 주소 기반 자동 지오코딩</p>
 * <p>imageUrl은 별도의 StoreImage API로 관리</p>
 */
public record UpdateStoreServiceRequest(
        String name,
        Long categoryId,
        String address,
        String lotNumberAddress,
        String phoneNumber,
        String description,
        Integer averagePrice,
        StoreType storeType
) {
    public static UpdateStoreServiceRequest of(
            String name,
            Long categoryId,
            String address,
            String lotNumberAddress,
            String phoneNumber,
            String description,
            Integer averagePrice,
            StoreType storeType
    ) {
        return new UpdateStoreServiceRequest(
                name,
                categoryId,
                address,
                lotNumberAddress,
                phoneNumber,
                description,
                averagePrice,
                storeType
        );
    }
}

