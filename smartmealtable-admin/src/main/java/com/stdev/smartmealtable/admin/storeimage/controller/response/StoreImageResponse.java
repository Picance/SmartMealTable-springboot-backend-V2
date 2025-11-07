package com.stdev.smartmealtable.admin.storeimage.controller.response;

import com.stdev.smartmealtable.domain.store.StoreImage;

/**
 * 가게 이미지 응답 DTO
 */
public record StoreImageResponse(
        Long storeImageId,
        Long storeId,
        String imageUrl,
        boolean isMain,
        Integer displayOrder
) {
    public static StoreImageResponse from(StoreImage storeImage) {
        return new StoreImageResponse(
                storeImage.getStoreImageId(),
                storeImage.getStoreId(),
                storeImage.getImageUrl(),
                storeImage.isMain(),
                storeImage.getDisplayOrder()
        );
    }
}
