package com.stdev.smartmealtable.domain.category;

import java.util.List;

/**
 * 카테고리 페이징 결과
 * 
 * <p>Spring Data의 Page를 대체하는 POJO 클래스</p>
 */
public record CategoryPageResult(
        List<Category> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    
    public static CategoryPageResult of(
            List<Category> content,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new CategoryPageResult(content, page, size, totalElements, totalPages);
    }
}
