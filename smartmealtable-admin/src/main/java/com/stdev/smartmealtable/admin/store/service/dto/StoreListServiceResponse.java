package com.stdev.smartmealtable.admin.store.service.dto;

import java.util.List;

/**
 * 음식점 목록 조회 Service Response
 */
public record StoreListServiceResponse(
        List<StoreServiceResponse> stores,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static StoreListServiceResponse of(
            List<StoreServiceResponse> stores,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        return new StoreListServiceResponse(
                stores,
                page,
                size,
                totalElements,
                totalPages
        );
    }
}
