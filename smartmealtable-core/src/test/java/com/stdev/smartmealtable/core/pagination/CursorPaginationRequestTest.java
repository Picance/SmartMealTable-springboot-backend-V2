package com.stdev.smartmealtable.core.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 커서 페이징 요청 테스트
 */
@DisplayName("CursorPaginationRequest 테스트")
class CursorPaginationRequestTest {

    @Test
    @DisplayName("커서 기반 페이징 - lastId가 있으면 커서 방식 사용")
    void testUseCursorPaginationWithLastId() {
        // Given
        CursorPaginationRequest request = CursorPaginationRequest.builder()
                .lastId(42L)
                .limit(20)
                .build();

        // Then
        assertThat(request.useCursorPagination()).isTrue();
        assertThat(request.useOffsetPagination()).isFalse();
    }

    @Test
    @DisplayName("커서 기반 페이징 - page/size가 없으면 커서 방식 사용")
    void testUseCursorPaginationWithoutPageSize() {
        // Given
        CursorPaginationRequest request = CursorPaginationRequest.builder()
                .lastId(null)
                .limit(20)
                .build();

        // Then
        assertThat(request.useCursorPagination()).isTrue();
        assertThat(request.useOffsetPagination()).isFalse();
    }

    @Test
    @DisplayName("오프셋 기반 페이징 - page가 제공되면 오프셋 방식 사용")
    void testUseOffsetPaginationWithPage() {
        // Given
        CursorPaginationRequest request = CursorPaginationRequest.builder()
                .page(0)
                .size(20)
                .build();

        // Then
        assertThat(request.useCursorPagination()).isFalse();
        assertThat(request.useOffsetPagination()).isTrue();
    }

    @Test
    @DisplayName("유효한 limit 값 검증")
    void testValidLimitValue() {
        // Given
        CursorPaginationRequest request = CursorPaginationRequest.builder()
                .limit(50)
                .build();

        // Then
        assertThat(request.getLimit()).isEqualTo(50);
    }

    @Test
    @DisplayName("기본 limit 값 확인 (20)")
    void testDefaultLimitValue() {
        // Given
        CursorPaginationRequest request = CursorPaginationRequest.builder()
                .build();

        // Then
        assertThat(request.getLimit()).isEqualTo(20);
    }

    @Test
    @DisplayName("getEffectiveLimit - 커서 방식")
    void testGetEffectiveLimitForCursor() {
        // Given
        CursorPaginationRequest request = CursorPaginationRequest.builder()
                .limit(30)
                .build();

        // Then
        assertThat(request.getEffectiveLimit()).isEqualTo(30);
    }

    @Test
    @DisplayName("getEffectiveLimit - 오프셋 방식")
    void testGetEffectiveLimitForOffset() {
        // Given
        CursorPaginationRequest request = CursorPaginationRequest.builder()
                .page(0)
                .size(40)
                .build();

        // Then
        assertThat(request.getEffectiveLimit()).isEqualTo(40);
    }
}
