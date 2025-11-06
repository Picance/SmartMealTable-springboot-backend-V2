package com.stdev.smartmealtable.core.pagination;

/**
 * 커서 페이징에서 ID를 제공하는 인터페이스
 *
 * <p>CursorPaginationResponse에서 마지막 ID를 추출하기 위해 구현해야 합니다.</p>
 *
 * @author Backend Team
 * @since 1.0.0
 */
public interface CursorIdentifiable {

    /**
     * 커서로 사용할 ID 반환
     *
     * @return 커서 ID
     */
    Long getCursorId();
}
