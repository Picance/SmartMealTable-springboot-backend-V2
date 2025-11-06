package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.store.StoreType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 가게 목록 조회 요청 DTO
 * 
 * <p>커서 기반 페이징과 오프셋 기반 페이징 모두 지원합니다.</p>
 * <ul>
 *   <li>커서 기반: lastId + limit 사용 (lastId가 제공되면 자동으로 커서 모드)</li>
 *   <li>오프셋 기반: page + size 사용 (기존 방식)</li>
 * </ul>
 */
public record StoreListRequest(
        String keyword,
        Double radius,
        Long categoryId,
        Boolean isOpen,
        StoreType storeType,
        String sortBy,
        Long lastId,
        @Min(1) @Max(100)
        Integer limit,
        Integer page,
        Integer size
) {
    public StoreListRequest {
        // 기본값 설정
        radius = (radius == null) ? 3.0 : radius;
        sortBy = (sortBy == null) ? "distance" : sortBy;
        page = (page == null || page < 0) ? 0 : page;
        size = (size == null || size < 1) ? 20 : size;
        limit = (limit == null || limit < 1) ? 20 : limit;
    }

    /**
     * 커서 기반 페이징 사용 여부 확인
     *
     * @return true if cursor-based pagination should be used
     */
    public boolean useCursorPagination() {
        return lastId != null || (page == null && size == null);
    }

    /**
     * 오프셋 페이징 사용 여부 확인 (하위 호환성)
     *
     * @return true if offset-based pagination should be used
     */
    public boolean useOffsetPagination() {
        return !useCursorPagination();
    }

    /**
     * 조회할 아이템 수 반환 (페이징 모드에 따라)
     *
     * @return limit (커서 모드) 또는 size (오프셋 모드)
     */
    public int getEffectiveLimit() {
        return useCursorPagination() ? limit : size;
    }

    /**
     * 현재 페이지 번호 반환 (오프셋 모드용)
     *
     * @return page (오프셋 모드) 또는 0 (커서 모드)
     */
    public int getEffectivePage() {
        return useOffsetPagination() ? page : 0;
    }
}
