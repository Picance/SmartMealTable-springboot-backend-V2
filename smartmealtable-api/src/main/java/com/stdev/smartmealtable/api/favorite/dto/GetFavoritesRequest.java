package com.stdev.smartmealtable.api.favorite.dto;

import lombok.Builder;

import java.util.Locale;

import static java.util.Set.of;

/**
 * 즐겨찾기 목록 조회 요청 DTO (커서 기반)
 */
@Builder
public record GetFavoritesRequest(
        String sortBy,
        Boolean isOpenOnly,
        Long categoryId,
        Long cursor,
        Integer size
) {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;
    private static final java.util.Set<String> SUPPORTED_SORTS = of(
            "priority",
            "name",
            "reviewcount",
            "distance",
            "createdat"
    );

    /**
     * 정렬 키 (소문자)
     */
    public String sortKey() {
        if (sortBy == null || sortBy.isBlank()) {
            return "priority";
        }
        return sortBy.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 지원되지 않는 정렬 키인지 확인
     */
    public boolean isUnsupportedSort() {
        return !SUPPORTED_SORTS.contains(sortKey());
    }

    /**
     * 영업 중 필터 사용 여부
     */
    public boolean openOnly() {
        return Boolean.TRUE.equals(isOpenOnly);
    }

    /**
     * 실제 사용될 페이지 크기 (1 ~ 50)
     */
    public int pageSize() {
        int resolved = (size == null) ? DEFAULT_SIZE : size;
        if (resolved < 1) {
            resolved = 1;
        }
        return Math.min(resolved, MAX_SIZE);
    }
}
