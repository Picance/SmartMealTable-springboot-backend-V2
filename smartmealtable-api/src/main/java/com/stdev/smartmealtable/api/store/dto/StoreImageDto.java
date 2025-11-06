package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.store.StoreImage;

/**
 * 가게 이미지 정보 DTO
 */
public record StoreImageDto(
        Long storeImageId,
        String imageUrl,
        Boolean isMain,
        Integer displayOrder
) {
    public static StoreImageDto from(StoreImage storeImage) {
        return new StoreImageDto(
                storeImage.getStoreImageId(),
                storeImage.getImageUrl(),
                storeImage.isMain(),
                storeImage.getDisplayOrder()
        );
    }
}
