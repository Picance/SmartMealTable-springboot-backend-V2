package com.stdev.smartmealtable.admin.store.service.dto;

import com.stdev.smartmealtable.domain.store.StoreType;

import java.util.List;

/**
 * 음식점 생성 Service Request - v2.0
 * 
 * <p>위도/경도는 제거되고, 주소를 기반으로 서버에서 지오코딩됩니다.</p>
 * <p>이미지는 별도 StoreImage API로 관리됩니다.</p>
 */
public record CreateStoreServiceRequest(
        String name,
        List<Long> categoryIds,
        Long sellerId,
        String address,
        String lotNumberAddress,
        String phoneNumber,
        String description,
        Integer averagePrice,
        StoreType storeType
) {
    public static CreateStoreServiceRequest of(
            String name,
            List<Long> categoryIds,
            Long sellerId,
            String address,
            String lotNumberAddress,
            String phoneNumber,
            String description,
            Integer averagePrice,
            StoreType storeType
    ) {
        return new CreateStoreServiceRequest(
                name,
                categoryIds,
                sellerId,
                address,
                lotNumberAddress,
                phoneNumber,
                description,
                averagePrice,
                storeType
        );
    }
}
