package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.store.StoreType;

/**
 * 가게 목록 조회 요청 DTO
 */
public record StoreListRequest(
        String keyword,
        Double radius,
        Long categoryId,
        Boolean isOpen,
        StoreType storeType,
        String sortBy,
        Integer page,
        Integer size
) {
    public StoreListRequest {
        // 기본값 설정
        radius = (radius == null) ? 3.0 : radius;
        sortBy = (sortBy == null) ? "distance" : sortBy;
        page = (page == null || page < 0) ? 0 : page;
        size = (size == null || size < 1) ? 20 : size;
    }
}
