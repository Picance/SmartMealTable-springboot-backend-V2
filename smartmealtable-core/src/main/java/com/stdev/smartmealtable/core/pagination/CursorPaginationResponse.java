package com.stdev.smartmealtable.core.pagination;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 커서 기반 페이징 응답 DTO
 *
 * <p>무한 스크롤 방식의 응답을 제공합니다.</p>
 *
 * @param <T> 응답 데이터 타입
 * @author Backend Team
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPaginationResponse<T> {

    /**
     * 조회한 데이터 목록
     */
    private List<T> data;

    /**
     * 다음 페이지 존재 여부
     *
     * <p>true: 더 이상 조회할 데이터가 있음</p>
     * <p>false: 마지막 페이지</p>
     */
    @JsonProperty("hasMore")
    private Boolean hasMore;

    /**
     * 마지막 항목의 ID (다음 커서)
     *
     * <p>다음 조회 시 이 값을 lastId 파라미터로 사용</p>
     */
    private Long lastId;

    /**
     * 현재 페이지의 항목 수
     */
    private Integer count;

    /**
     * 페이지 번호 (하위 호환성 지원)
     *
     * <p>오프셋 기반 페이징 응답 시 사용</p>
     */
    private Integer page;

    /**
     * 페이지 크기 (하위 호환성 지원)
     *
     * <p>오프셋 기반 페이징 응답 시 사용</p>
     */
    private Integer size;

    /**
     * 전체 항목 수 (하위 호환성 지원)
     *
     * <p>오프셋 기반 페이징 응답 시 사용</p>
     */
    private Long totalCount;

    /**
     * 커서 기반 페이징 응답 생성 (무한 스크롤용)
     *
     * @param data 조회한 데이터 목록
     * @param hasMore 다음 페이지 존재 여부
     * @param limit 요청한 항목 수
     * @return 커서 기반 페이징 응답
     * @param <T> 응답 데이터 타입
     */
    public static <T> CursorPaginationResponse<T> of(List<T> data, Boolean hasMore, Integer limit) {
        Long lastId = null;
        if (!data.isEmpty()) {
            // data의 마지막 항목에서 ID를 추출 (CursorIdentifiable 인터페이스 가정)
            // 실제 구현에서는 제네릭 T가 ID를 제공해야 함
            lastId = extractLastId(data);
        }

        return CursorPaginationResponse.<T>builder()
                .data(data)
                .hasMore(hasMore)
                .lastId(lastId)
                .count(data.size())
                .build();
    }

    /**
     * 오프셋 기반 페이징 응답 생성 (하위 호환성)
     *
     * @param data 조회한 데이터 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param totalCount 전체 항목 수
     * @return 오프셋 기반 페이징 응답
     * @param <T> 응답 데이터 타입
     */
    public static <T> CursorPaginationResponse<T> ofOffset(List<T> data, Integer page, Integer size, Long totalCount) {
        boolean hasMore = (long) (page + 1) * size < totalCount;

        Long lastId = null;
        if (!data.isEmpty()) {
            lastId = extractLastId(data);
        }

        return CursorPaginationResponse.<T>builder()
                .data(data)
                .hasMore(hasMore)
                .lastId(lastId)
                .count(data.size())
                .page(page)
                .size(size)
                .totalCount(totalCount)
                .build();
    }

    /**
     * 데이터 목록의 마지막 항목에서 ID 추출
     *
     * <p>T가 CursorIdentifiable 인터페이스를 구현해야 합니다.</p>
     *
     * @param data 데이터 목록
     * @return 마지막 항목의 ID (null if list is empty)
     * @param <T> 응답 데이터 타입
     */
    @SuppressWarnings("unchecked")
    private static <T> Long extractLastId(List<T> data) {
        if (data.isEmpty()) {
            return null;
        }

        T lastItem = data.get(data.size() - 1);

        // CursorIdentifiable 인터페이스 확인
        if (lastItem instanceof CursorIdentifiable) {
            return ((CursorIdentifiable) lastItem).getCursorId();
        }

        // 만약 ID 필드를 직접 가지고 있다면 반사를 사용 (권장하지 않음)
        try {
            var idField = lastItem.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object idValue = idField.get(lastItem);
            if (idValue instanceof Long) {
                return (Long) idValue;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // ID 필드가 없으면 null 반환
        }

        return null;
    }
}
