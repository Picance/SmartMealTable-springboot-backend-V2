package com.stdev.smartmealtable.api.budget.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * UpdateDailyBudgetService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateDailyBudgetService 테스트")
class UpdateDailyBudgetServiceTest {

    @Mock
    private DailyBudgetRepository dailyBudgetRepository;

    @InjectMocks
    private UpdateDailyBudgetService updateDailyBudgetService;

    @Nested
    @DisplayName("updateDailyBudget 메서드는")
    class Describe_updateDailyBudget {

        @Nested
        @DisplayName("applyForward가 false이고 해당 날짜의 예산이 존재하면")
        class Context_with_applyForward_false_and_existing_budget {

            @Test
            @DisplayName("해당 날짜의 예산만 수정한다")
            void it_updates_only_specified_date_budget() {
                // Given
                Long memberId = 1L;
                LocalDate date = LocalDate.of(2025, 10, 16);
                Integer newBudget = 15000;
                Boolean applyForward = false;

                DailyBudget dailyBudget = DailyBudget.create(memberId, 10000, date);

                given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, date))
                        .willReturn(Optional.of(dailyBudget));
                given(dailyBudgetRepository.save(any(DailyBudget.class)))
                        .willReturn(dailyBudget);

                // When
                UpdateDailyBudgetServiceResponse response = updateDailyBudgetService.updateDailyBudget(
                        memberId, date, newBudget, applyForward
                );

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getUpdatedCount()).isEqualTo(1);
                assertThat(response.getDailyFoodBudget()).isEqualTo(newBudget);
                assertThat(response.getAppliedForward()).isFalse();

                then(dailyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetDate(memberId, date);
                then(dailyBudgetRepository).should(times(1)).save(any(DailyBudget.class));
                then(dailyBudgetRepository).should(times(0))
                        .findByMemberIdAndBudgetDateGreaterThanEqual(any(), any());
            }
        }

        @Nested
        @DisplayName("applyForward가 true이고 이후 날짜의 예산이 여러 개 존재하면")
        class Context_with_applyForward_true_and_multiple_future_budgets {

            @Test
            @DisplayName("해당 날짜 이후의 모든 예산을 수정한다")
            void it_updates_all_future_budgets() {
                // Given
                Long memberId = 1L;
                LocalDate date = LocalDate.of(2025, 10, 16);
                Integer newBudget = 15000;
                Boolean applyForward = true;

                DailyBudget dailyBudget = DailyBudget.create(memberId, 10000, date);
                DailyBudget futureBudget1 = DailyBudget.create(memberId, 10000, date);
                DailyBudget futureBudget2 = DailyBudget.create(memberId, 10000, date.plusDays(1));
                DailyBudget futureBudget3 = DailyBudget.create(memberId, 10000, date.plusDays(2));

                List<DailyBudget> futureBudgets = List.of(futureBudget1, futureBudget2, futureBudget3);

                given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, date))
                        .willReturn(Optional.of(dailyBudget));
                given(dailyBudgetRepository.findByMemberIdAndBudgetDateGreaterThanEqual(memberId, date))
                        .willReturn(futureBudgets);
                given(dailyBudgetRepository.save(any(DailyBudget.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                UpdateDailyBudgetServiceResponse response = updateDailyBudgetService.updateDailyBudget(
                        memberId, date, newBudget, applyForward
                );

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getUpdatedCount()).isEqualTo(3);
                assertThat(response.getDailyFoodBudget()).isEqualTo(newBudget);
                assertThat(response.getAppliedForward()).isTrue();

                then(dailyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetDate(memberId, date);
                then(dailyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetDateGreaterThanEqual(memberId, date);
                then(dailyBudgetRepository).should(times(3)).save(any(DailyBudget.class));
            }
        }

        @Nested
        @DisplayName("applyForward가 true이지만 이후 날짜의 예산이 없으면")
        class Context_with_applyForward_true_but_no_future_budgets {

            @Test
            @DisplayName("업데이트된 예산 개수가 0이다")
            void it_updates_zero_budgets() {
                // Given
                Long memberId = 1L;
                LocalDate date = LocalDate.of(2025, 10, 16);
                Integer newBudget = 15000;
                Boolean applyForward = true;

                DailyBudget dailyBudget = DailyBudget.create(memberId, 10000, date);

                given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, date))
                        .willReturn(Optional.of(dailyBudget));
                given(dailyBudgetRepository.findByMemberIdAndBudgetDateGreaterThanEqual(memberId, date))
                        .willReturn(List.of());

                // When
                UpdateDailyBudgetServiceResponse response = updateDailyBudgetService.updateDailyBudget(
                        memberId, date, newBudget, applyForward
                );

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getUpdatedCount()).isEqualTo(0);
                assertThat(response.getDailyFoodBudget()).isEqualTo(newBudget);
                assertThat(response.getAppliedForward()).isTrue();

                then(dailyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetDate(memberId, date);
                then(dailyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetDateGreaterThanEqual(memberId, date);
                then(dailyBudgetRepository).should(times(0)).save(any(DailyBudget.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 날짜의 예산을 수정하려고 하면")
        class Context_with_non_existing_budget {

            @Test
            @DisplayName("DAILY_BUDGET_NOT_FOUND 예외를 발생시킨다")
            void it_throws_daily_budget_not_found_exception() {
                // Given
                Long memberId = 1L;
                LocalDate date = LocalDate.of(2025, 10, 16);
                Integer newBudget = 15000;
                Boolean applyForward = false;

                given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, date))
                        .willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> updateDailyBudgetService.updateDailyBudget(
                        memberId, date, newBudget, applyForward
                ))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.DAILY_BUDGET_NOT_FOUND);

                then(dailyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetDate(memberId, date);
                then(dailyBudgetRepository).should(times(0)).save(any(DailyBudget.class));
                then(dailyBudgetRepository).should(times(0))
                        .findByMemberIdAndBudgetDateGreaterThanEqual(any(), any());
            }
        }

        @Nested
        @DisplayName("예산 금액을 0원으로 수정하려고 하면")
        class Context_with_zero_budget {

            @Test
            @DisplayName("예산을 0원으로 수정한다")
            void it_updates_budget_to_zero() {
                // Given
                Long memberId = 1L;
                LocalDate date = LocalDate.of(2025, 10, 16);
                Integer newBudget = 0;
                Boolean applyForward = false;

                DailyBudget dailyBudget = DailyBudget.create(memberId, 10000, date);

                given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, date))
                        .willReturn(Optional.of(dailyBudget));
                given(dailyBudgetRepository.save(any(DailyBudget.class)))
                        .willReturn(dailyBudget);

                // When
                UpdateDailyBudgetServiceResponse response = updateDailyBudgetService.updateDailyBudget(
                        memberId, date, newBudget, applyForward
                );

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getUpdatedCount()).isEqualTo(1);
                assertThat(response.getDailyFoodBudget()).isEqualTo(0);

                then(dailyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetDate(memberId, date);
                then(dailyBudgetRepository).should(times(1)).save(any(DailyBudget.class));
            }
        }
    }
}
