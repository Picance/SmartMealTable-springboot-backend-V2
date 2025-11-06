package com.stdev.smartmealtable.core.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * 커서 페이징 응답 테스트
 */
@DisplayName("CursorPaginationResponse 테스트")
class CursorPaginationResponseTest {

    /**
     * 테스트 데이터 객체
     */
    static class TestData implements CursorIdentifiable {
        private final Long id;
        private final String name;

        TestData(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public Long getCursorId() {
            return id;
        }
    }

    @Test
    @DisplayName("커서 기반 응답 생성 - hasMore 계산")
    void testCreateCursorResponse() {
        // Given
        List<TestData> data = Arrays.asList(
                new TestData(1L, "Item 1"),
                new TestData(2L, "Item 2"),
                new TestData(3L, "Item 3")
        );

        // When
        CursorPaginationResponse<TestData> response = CursorPaginationResponse.of(data, true, 20);

        // Then
        assertThat(response.getData()).hasSize(3);
        assertThat(response.getHasMore()).isTrue();
        assertThat(response.getLastId()).isEqualTo(3L);
        assertThat(response.getCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("빈 리스트 응답")
    void testEmptyListResponse() {
        // Given
        List<TestData> data = List.of();

        // When
        CursorPaginationResponse<TestData> response = CursorPaginationResponse.of(data, false, 20);

        // Then
        assertThat(response.getData()).isEmpty();
        assertThat(response.getHasMore()).isFalse();
        assertThat(response.getLastId()).isNull();
        assertThat(response.getCount()).isZero();
    }

    @Test
    @DisplayName("오프셋 기반 응답 생성")
    void testCreateOffsetResponse() {
        // Given
        List<TestData> data = Arrays.asList(
                new TestData(10L, "Page 1 Item 1"),
                new TestData(11L, "Page 1 Item 2")
        );
        long totalCount = 50;

        // When
        CursorPaginationResponse<TestData> response = CursorPaginationResponse.ofOffset(
                data, 0, 2, totalCount
        );

        // Then
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(2);
        assertThat(response.getTotalCount()).isEqualTo(50);
        assertThat(response.getHasMore()).isTrue();
        assertThat(response.getLastId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("오프셋 기반 응답 - hasMore = false (마지막 페이지)")
    void testOffsetResponseLastPage() {
        // Given
        List<TestData> data = Arrays.asList(
                new TestData(49L, "Last Item 1"),
                new TestData(50L, "Last Item 2")
        );
        long totalCount = 50;

        // When
        CursorPaginationResponse<TestData> response = CursorPaginationResponse.ofOffset(
                data, 24, 2, totalCount
        );

        // Then
        assertThat(response.getHasMore()).isFalse();
    }
}
