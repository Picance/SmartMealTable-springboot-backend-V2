package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.budget.service.DailyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.dto.DailyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureStatisticsServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetExpenditureStatisticsService")
class GetExpenditureStatisticsServiceTest {

    @Mock
    private ExpenditureRepository expenditureRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DailyBudgetQueryService dailyBudgetQueryService;

    @InjectMocks
    private GetExpenditureStatisticsService getExpenditureStatisticsService;

    @Test
    @DisplayName("등록되지 않은 날짜가 포함되어도 일별 통계가 반환된다")
    void getStatistics_handlesUnregisteredDatesWithoutException() {
        // given
        Long memberId = 1L;
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = startDate.plusDays(1);

        Map<LocalDate, Long> dailyAmounts = Map.of(
                startDate, 1_000L,
                endDate, 2_000L
        );

        given(expenditureRepository.getTotalAmountByPeriod(memberId, startDate, endDate))
                .willReturn(3_000L);
        given(expenditureRepository.getAmountByCategoryForPeriod(memberId, startDate, endDate))
                .willReturn(Map.of());
        given(expenditureRepository.getDailyAmountForPeriod(memberId, startDate, endDate))
                .willReturn(dailyAmounts);
        given(expenditureRepository.getAmountByMealTypeForPeriod(memberId, startDate, endDate))
                .willReturn(Map.of());

        given(dailyBudgetQueryService.getDailyBudget(memberId, startDate))
                .willThrow(new BusinessException(ErrorType.DAILY_BUDGET_NOT_FOUND));

        given(dailyBudgetQueryService.getDailyBudget(memberId, endDate))
                .willReturn(new DailyBudgetQueryServiceResponse(
                        endDate,
                        10_000,
                        2_000,
                        8_000,
                        List.of()
                ));

        // when
        ExpenditureStatisticsServiceResponse response = getExpenditureStatisticsService.getStatistics(
                memberId,
                startDate,
                endDate
        );

        // then
        assertThat(response.dailyStatistics()).hasSize(2);

        ExpenditureStatisticsServiceResponse.DailyStatistics first = response.dailyStatistics().get(0);
        assertThat(first.date()).isEqualTo(startDate);
        assertThat(first.totalSpentAmount()).isEqualTo(1_000L);
        assertThat(first.budgetRegistered()).isFalse();
        assertThat(first.budget()).isNull();
        assertThat(first.balance()).isNull();
        assertThat(first.overBudget()).isNull();

        ExpenditureStatisticsServiceResponse.DailyStatistics second = response.dailyStatistics().get(1);
        assertThat(second.date()).isEqualTo(endDate);
        assertThat(second.totalSpentAmount()).isEqualTo(2_000L);
        assertThat(second.budgetRegistered()).isTrue();
        assertThat(second.budget()).isEqualTo(10_000L);
        assertThat(second.balance()).isEqualTo(8_000L);
        assertThat(second.overBudget()).isFalse();
    }
}
