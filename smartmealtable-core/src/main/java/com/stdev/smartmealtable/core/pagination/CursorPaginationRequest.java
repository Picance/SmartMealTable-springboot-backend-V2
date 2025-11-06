package com.stdev.smartmealtable.core.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커서 기반 페이징 요청 DTO
 *
 * <p>커서(마지막 항목 ID)를 기준으로 다음 데이터를 조회하는 무한 스크롤 방식을 지원합니다.</p>
 *
 * @author Backend Team
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPaginationRequest {

    /**
     * 마지막 항목의 ID (커서)
     *
     * <p>null인 경우 처음부터 조회합니다.</p>
     */
    private Long lastId;

    /**
     * 조회할 항목 수 (limit)
     *
     * <p>기본값: 20, 최대값: 100</p>
     */
    @Min(value = 1, message = "조회 항목 수는 1 이상이어야 합니다")
    @Max(value = 100, message = "조회 항목 수는 100 이하여야 합니다")
    @Builder.Default
    private Integer limit = 20;

    /**
     * 페이지 번호 (하위 호환성 지원)
     *
     * <p>기본값: null (커서 방식 사용)</p>
     *
     * <p>page가 제공되면 page/size 기반 페이징으로 동작합니다.</p>
     */
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private Integer page;

    /**
     * 페이지 크기 (하위 호환성 지원)
     *
     * <p>기본값: null (커서 방식 사용)</p>
     *
     * <p>size가 제공되면 page/size 기반 페이징으로 동작합니다.</p>
     */
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    private Integer size;

    /**
     * 커서 방식을 사용해야 하는지 확인
     *
     * @return true if cursor-based pagination should be used
     */
    public boolean useCursorPagination() {
        return page == null && size == null;
    }

    /**
     * 오프셋 페이징을 사용해야 하는지 확인 (하위 호환성)
     *
     * @return true if offset-based pagination should be used
     */
    public boolean useOffsetPagination() {
        return page != null || size != null;
    }

    /**
     * 조회할 항목 수를 반환 (오프셋 페이징의 경우 size 사용)
     *
     * @return 조회할 항목 수
     */
    public Integer getEffectiveLimit() {
        if (useOffsetPagination()) {
            return size != null ? size : 20;
        }
        return limit != null ? limit : 20;
    }

    /**
     * 오프셋 페이징의 페이지를 반환
     *
     * @return 페이지 번호 (기본값: 0)
     */
    public Integer getEffectivePage() {
        return page != null ? page : 0;
    }
}
