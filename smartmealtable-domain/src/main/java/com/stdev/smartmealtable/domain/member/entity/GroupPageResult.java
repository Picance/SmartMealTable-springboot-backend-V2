package com.stdev.smartmealtable.domain.member.entity;

import java.util.List;

/**
 * Group 페이징 결과 (POJO)
 * Spring Data의 Page를 대체하여 도메인 순수성 유지
 */
public record GroupPageResult(
        List<Group> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static GroupPageResult of(
            List<Group> content,
            int pageNumber,
            int pageSize,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean hasNext = pageNumber < totalPages - 1;
        
        return new GroupPageResult(
                content,
                pageNumber,
                pageSize,
                totalElements,
                totalPages,
                hasNext
        );
    }
}
