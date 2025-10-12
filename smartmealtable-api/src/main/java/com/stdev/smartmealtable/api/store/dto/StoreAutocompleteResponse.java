package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.store.Store;

/**
 * 가게 자동완성 검색 응답 DTO
 */
public record StoreAutocompleteResponse(
        Long storeId,
        String name,
        String address,
        String categoryName
) {
    public static StoreAutocompleteResponse from(Store store) {
        return new StoreAutocompleteResponse(
                store.getStoreId(),
                store.getName(),
                store.getAddress(),
                null // TODO: Category 조인 필요
        );
    }
}
