package com.stdev.smartmealtable.domain.store;

import java.util.List;

/**
 * Store 페이징 결과 (POJO)
 * Spring Data의 Page를 대체하여 도메인 순수성 유지
 */
public record StorePageResult(
        List<Store> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static StorePageResult of(
            List<Store> content,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        
        return new StorePageResult(
                content,
                page,
                size,
                totalElements,
                totalPages,
                hasNext
        );
    }
}
