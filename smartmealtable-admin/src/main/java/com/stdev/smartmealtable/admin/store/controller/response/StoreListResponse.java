package com.stdev.smartmealtable.admin.store.controller.response;

import com.stdev.smartmealtable.admin.store.service.dto.StoreListServiceResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 음식점 목록 조회 Response DTO (Controller)
 */
public record StoreListResponse(
        List<StoreResponse> stores,
        PageInfo pageInfo
) {
    public static StoreListResponse from(StoreListServiceResponse serviceResponse) {
        List<StoreResponse> stores = serviceResponse.stores().stream()
                .map(StoreResponse::from)
                .collect(Collectors.toList());

        PageInfo pageInfo = new PageInfo(
                serviceResponse.page(),
                serviceResponse.size(),
                serviceResponse.totalElements(),
                serviceResponse.totalPages()
        );

        return new StoreListResponse(stores, pageInfo);
    }

    public record PageInfo(
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
