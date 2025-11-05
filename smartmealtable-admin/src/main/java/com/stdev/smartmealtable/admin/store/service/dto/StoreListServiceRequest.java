package com.stdev.smartmealtable.admin.store.service.dto;

import com.stdev.smartmealtable.domain.store.StoreType;

/**
 * 관리자용 음식점 목록 조회 Service Request
 */
public record StoreListServiceRequest(
        Long categoryId,
        String name,
        StoreType storeType,
        Integer page,
        Integer size
) {
    public static StoreListServiceRequest of(
            Long categoryId,
            String name,
            StoreType storeType,
            Integer page,
            Integer size
    ) {
        return new StoreListServiceRequest(
                categoryId,
                name,
                storeType,
                page != null ? page : 0,
                size != null ? size : 20
        );
    }
}
