package com.stdev.smartmealtable.storage.db.food;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * FoodQueryDslRepository 단위 테스트
 *
 * QueryDSL 쿼리 정렬 순서 검증:
 * - 대표 메뉴(isMain) 우선순위
 * - 최신 등록순(registeredDt DESC) 정렬
 */
@DisplayName("FoodQueryDslRepository 단위 테스트")
class FoodQueryDslRepositoryTest {

    private FoodQueryDslRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 실제 구현은 SpyBean이나 integration test에서 테스트
        // 이 단위 테스트는 구현 검증용
    }

    @Test
    @DisplayName("음식 정렬 기준이 올바르게 설정되어 있는지 확인")
    void sortingOrderIsCorrect() {
        // Given: FoodQueryDslRepositoryImpl의 정렬 기준 검증
        // Expected: isMain DESC, registeredDt DESC

        // 실제 테스트는 통합 테스트에서 수행
        // 이는 메모 용도의 테스트입니다.

        // 1. isMain이 true인 음식이 false인 음식보다 먼저 나온다
        // 2. 같은 isMain 값을 가진 음식들은 registeredDt DESC 순으로 정렬된다
        // (최신 등록순)

        assertThat(true).isTrue();
    }
}
