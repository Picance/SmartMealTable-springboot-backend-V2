package com.stdev.smartmealtable.domain.budget.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.budget.*;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * BudgetDomainService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetDomainService 테스트")
class BudgetDomainServiceTest {

    @Mock
    private MonthlyBudgetRepository monthlyBudgetRepository;

    @Mock
    private DailyBudgetRepository dailyBudgetRepository;

    @Mock
    private MealBudgetRepository mealBudgetRepository;

    @InjectMocks
    private BudgetDomainService budgetDomainService;

    @Nested
    @DisplayName("setupInitialBudget 메서드는")
    class Describe_setupInitialBudget {

        @Nested
        @DisplayName("유효한 예산 정보가 주어지면")
        class Context_with_valid_budget_info {

            @Test
            @DisplayName("월별 예산과 오늘부터 월말까지의 일별, 식사별 예산을 생성하고 저장한다")
            void it_creates_and_saves_all_budgets() {
                // Given
                Long memberId = 1L;
                Integer monthlyAmount = 300000;
                Integer dailyAmount = 10000;
                Map<MealType, Integer> mealBudgets = Map.of(
                        MealType.BREAKFAST, 3000,
                        MealType.LUNCH, 4000,
                        MealType.DINNER, 3000
                );

                YearMonth currentMonth = YearMonth.now();
                String budgetMonth = currentMonth.toString();
                LocalDate today = LocalDate.now();
                LocalDate endOfMonth = currentMonth.atEndOfMonth();

                // 오늘부터 월말까지의 일 수 계산
                long dayCount = java.time.temporal.ChronoUnit.DAYS.between(today, endOfMonth) + 1;

                MonthlyBudget monthlyBudget = MonthlyBudget.create(memberId, monthlyAmount, budgetMonth);
                DailyBudget dailyBudget = DailyBudget.create(memberId, dailyAmount, today);
                MealBudget breakfastBudget = MealBudget.create(1L, 3000, MealType.BREAKFAST, today);
                MealBudget lunchBudget = MealBudget.create(1L, 4000, MealType.LUNCH, today);
                MealBudget dinnerBudget = MealBudget.create(1L, 3000, MealType.DINNER, today);

                given(monthlyBudgetRepository.save(any(MonthlyBudget.class))).willReturn(monthlyBudget);
                given(dailyBudgetRepository.save(any(DailyBudget.class))).willReturn(dailyBudget);
                given(mealBudgetRepository.save(any(MealBudget.class)))
                        .willReturn(breakfastBudget, lunchBudget, dinnerBudget);

                // When
                BudgetDomainService.BudgetSetupResult result = budgetDomainService.setupInitialBudget(
                        memberId, monthlyAmount, dailyAmount, mealBudgets
                );

                // Then
                assertThat(result).isNotNull();
                assertThat(result.monthlyBudget()).isNotNull();
                assertThat(result.dailyBudget()).isNotNull();
                assertThat(result.mealBudgets()).hasSize((int) (dayCount * 3)); // 각 날짜마다 3개의 식사별 예산

                then(monthlyBudgetRepository).should(times(1)).save(any(MonthlyBudget.class));
                then(dailyBudgetRepository).should(times((int) dayCount)).save(any(DailyBudget.class));
                then(mealBudgetRepository).should(times((int) (dayCount * 3))).save(any(MealBudget.class));
            }
        }

        @Nested
        @DisplayName("빈 식사별 예산 맵이 주어지면")
        class Context_with_empty_meal_budgets {

            @Test
            @DisplayName("월별 예산과 오늘부터 월말까지의 일별 예산만 생성하고 식사별 예산은 생성하지 않는다")
            void it_creates_only_monthly_and_daily_budgets() {
                // Given
                Long memberId = 1L;
                Integer monthlyAmount = 300000;
                Integer dailyAmount = 10000;
                Map<MealType, Integer> emptyMealBudgets = Map.of();

                YearMonth currentMonth = YearMonth.now();
                String budgetMonth = currentMonth.toString();
                LocalDate today = LocalDate.now();
                LocalDate endOfMonth = currentMonth.atEndOfMonth();

                // 오늘부터 월말까지의 일 수 계산
                long dayCount = java.time.temporal.ChronoUnit.DAYS.between(today, endOfMonth) + 1;

                MonthlyBudget monthlyBudget = MonthlyBudget.create(memberId, monthlyAmount, budgetMonth);
                DailyBudget dailyBudget = DailyBudget.create(memberId, dailyAmount, today);

                given(monthlyBudgetRepository.save(any(MonthlyBudget.class))).willReturn(monthlyBudget);
                given(dailyBudgetRepository.save(any(DailyBudget.class))).willReturn(dailyBudget);

                // When
                BudgetDomainService.BudgetSetupResult result = budgetDomainService.setupInitialBudget(
                        memberId, monthlyAmount, dailyAmount, emptyMealBudgets
                );

                // Then
                assertThat(result).isNotNull();
                assertThat(result.monthlyBudget()).isNotNull();
                assertThat(result.dailyBudget()).isNotNull();
                assertThat(result.mealBudgets()).isEmpty();

                then(monthlyBudgetRepository).should(times(1)).save(any(MonthlyBudget.class));
                then(dailyBudgetRepository).should(times((int) dayCount)).save(any(DailyBudget.class));
                then(mealBudgetRepository).should(times(0)).save(any(MealBudget.class));
            }
        }
    }

    @Nested
    @DisplayName("updateMonthlyBudget 메서드는")
    class Describe_updateMonthlyBudget {

        @Nested
        @DisplayName("존재하는 월별 예산과 새 금액이 주어지면")
        class Context_with_existing_monthly_budget {

            @Test
            @DisplayName("월별 예산을 수정하고 저장한다")
            void it_updates_and_saves_monthly_budget() {
                // Given
                Long memberId = 1L;
                String month = "2025-10";
                Integer newAmount = 400000;

                MonthlyBudget existingBudget = MonthlyBudget.create(memberId, 300000, month);
                MonthlyBudget updatedBudget = MonthlyBudget.create(memberId, newAmount, month);

                given(monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, month))
                        .willReturn(Optional.of(existingBudget));
                given(monthlyBudgetRepository.save(any(MonthlyBudget.class)))
                        .willReturn(updatedBudget);

                // When
                MonthlyBudget result = budgetDomainService.updateMonthlyBudget(memberId, month, newAmount);

                // Then
                assertThat(result).isNotNull();
                then(monthlyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetMonth(memberId, month);
                then(monthlyBudgetRepository).should(times(1)).save(any(MonthlyBudget.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 월별 예산이 주어지면")
        class Context_with_non_existing_monthly_budget {

            @Test
            @DisplayName("MONTHLY_BUDGET_NOT_FOUND 예외를 발생시킨다")
            void it_throws_monthly_budget_not_found_exception() {
                // Given
                Long memberId = 1L;
                String month = "2025-10";
                Integer newAmount = 400000;

                given(monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, month))
                        .willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> budgetDomainService.updateMonthlyBudget(memberId, month, newAmount))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.MONTHLY_BUDGET_NOT_FOUND);

                then(monthlyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetMonth(memberId, month);
                then(monthlyBudgetRepository).should(times(0)).save(any(MonthlyBudget.class));
            }
        }
    }

    @Nested
    @DisplayName("updateDailyBudgetsInMonth 메서드는")
    class Describe_updateDailyBudgetsInMonth {

        @Nested
        @DisplayName("해당 월의 여러 일별 예산이 존재하면")
        class Context_with_multiple_daily_budgets_in_month {

            @Test
            @DisplayName("해당 월의 모든 일별 예산을 수정한다")
            void it_updates_all_daily_budgets_in_month() {
                // Given
                Long memberId = 1L;
                String month = "2025-10";
                Integer newAmount = 12000;

                YearMonth yearMonth = YearMonth.parse(month);
                LocalDate startOfMonth = yearMonth.atDay(1);
                LocalDate endOfMonth = yearMonth.atEndOfMonth();

                DailyBudget budget1 = DailyBudget.create(memberId, 10000, startOfMonth);
                DailyBudget budget2 = DailyBudget.create(memberId, 10000, startOfMonth.plusDays(1));
                DailyBudget budget3 = DailyBudget.create(memberId, 10000, startOfMonth.plusDays(2));
                List<DailyBudget> dailyBudgets = List.of(budget1, budget2, budget3);

                given(dailyBudgetRepository.findByMemberIdAndBudgetDateGreaterThanEqual(memberId, startOfMonth))
                        .willReturn(dailyBudgets);
                given(dailyBudgetRepository.save(any(DailyBudget.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                List<DailyBudget> result = budgetDomainService.updateDailyBudgetsInMonth(memberId, month, newAmount);

                // Then
                assertThat(result).hasSize(3);
                then(dailyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetDateGreaterThanEqual(memberId, startOfMonth);
                then(dailyBudgetRepository).should(times(3)).save(any(DailyBudget.class));
            }
        }

        @Nested
        @DisplayName("해당 월의 일별 예산이 없으면")
        class Context_with_no_daily_budgets_in_month {

            @Test
            @DisplayName("빈 리스트를 반환한다")
            void it_returns_empty_list() {
                // Given
                Long memberId = 1L;
                String month = "2025-10";
                Integer newAmount = 12000;

                YearMonth yearMonth = YearMonth.parse(month);
                LocalDate startOfMonth = yearMonth.atDay(1);

                given(dailyBudgetRepository.findByMemberIdAndBudgetDateGreaterThanEqual(memberId, startOfMonth))
                        .willReturn(List.of());

                // When
                List<DailyBudget> result = budgetDomainService.updateDailyBudgetsInMonth(memberId, month, newAmount);

                // Then
                assertThat(result).isEmpty();
                then(dailyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetDateGreaterThanEqual(memberId, startOfMonth);
                then(dailyBudgetRepository).should(times(0)).save(any(DailyBudget.class));
            }
        }

        @Nested
        @DisplayName("해당 월을 넘어서는 일별 예산이 포함되어 있으면")
        class Context_with_daily_budgets_exceeding_month {

            @Test
            @DisplayName("해당 월에 속하는 예산만 수정한다")
            void it_updates_only_budgets_within_month() {
                // Given
                Long memberId = 1L;
                String month = "2025-10";
                Integer newAmount = 12000;

                YearMonth yearMonth = YearMonth.parse(month);
                LocalDate startOfMonth = yearMonth.atDay(1);
                LocalDate endOfMonth = yearMonth.atEndOfMonth();
                LocalDate nextMonthDate = endOfMonth.plusDays(1);

                DailyBudget budgetInMonth1 = DailyBudget.create(memberId, 10000, startOfMonth);
                DailyBudget budgetInMonth2 = DailyBudget.create(memberId, 10000, endOfMonth);
                DailyBudget budgetNextMonth = DailyBudget.create(memberId, 10000, nextMonthDate);
                List<DailyBudget> dailyBudgets = List.of(budgetInMonth1, budgetInMonth2, budgetNextMonth);

                given(dailyBudgetRepository.findByMemberIdAndBudgetDateGreaterThanEqual(memberId, startOfMonth))
                        .willReturn(dailyBudgets);
                given(dailyBudgetRepository.save(any(DailyBudget.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                List<DailyBudget> result = budgetDomainService.updateDailyBudgetsInMonth(memberId, month, newAmount);

                // Then
                assertThat(result).hasSize(2); // 다음 달 예산은 제외
                then(dailyBudgetRepository).should(times(1))
                        .findByMemberIdAndBudgetDateGreaterThanEqual(memberId, startOfMonth);
                then(dailyBudgetRepository).should(times(2)).save(any(DailyBudget.class));
            }
        }
    }
}
