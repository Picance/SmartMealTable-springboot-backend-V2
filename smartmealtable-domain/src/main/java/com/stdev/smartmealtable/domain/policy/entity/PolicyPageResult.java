package com.stdev.smartmealtable.domain.policy.entity;

import java.util.List;

/**
 * 약관 페이징 결과
 * 
 * <p>Spring Data의 Page를 대체하는 POJO 클래스</p>
 */
public record PolicyPageResult(
        List<Policy> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    
    public static PolicyPageResult of(
            List<Policy> content,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PolicyPageResult(content, page, size, totalElements, totalPages);
    }
}
