package com.stdev.smartmealtable.core.api.response;

/**
 * 페이징 정보
 */
public record PageInfo(
        int page,
        int size,
        int totalElements,
        int totalPages,
        boolean last
) {
}
