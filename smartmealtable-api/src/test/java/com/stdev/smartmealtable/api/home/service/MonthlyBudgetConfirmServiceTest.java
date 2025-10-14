package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.api.home.service.dto.MonthlyBudgetConfirmServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.core.exception.ResourceNotFoundException;
import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import com.stdev.smartmealtable.domain.member.entity.BudgetConfirmAction;
import com.stdev.smartmealtable.domain.member.entity.MonthlyBudgetConfirmation;
import com.stdev.smartmealtable.domain.member.repository.MonthlyBudgetConfirmationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * MonthlyBudgetConfirmService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MonthlyBudgetConfirmService 단위 테스트")
class MonthlyBudgetConfirmServiceTest {

    @InjectMocks
    private MonthlyBudgetConfirmService monthlyBudgetConfirmService;

    @Mock
    private MonthlyBudgetConfirmationRepository confirmationRepository;

    @Mock
    private MonthlyBudgetRepository monthlyBudgetRepository;

    @Test
    @DisplayName("월별 예산 확인 - 성공 (KEEP 액션)")
    void confirmMonthlyBudget_Success_KeepAction() {
        // given
        Long memberId = 1L;
        Integer year = 2024;
        Integer month = 3;
        String action = "KEEP";
        String budgetMonth = "2024-03";
        Integer monthlyBudgetAmount = 500000;

        given(confirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month))
                .willReturn(false);

        MonthlyBudget monthlyBudget = MonthlyBudget.reconstitute(1L, memberId, monthlyBudgetAmount, 0, budgetMonth);
        given(monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth))
                .willReturn(Optional.of(monthlyBudget));

        MonthlyBudgetConfirmation savedConfirmation = MonthlyBudgetConfirmation.reconstitute(
                1L, memberId, year, month, BudgetConfirmAction.KEEP, LocalDateTime.now());
        given(confirmationRepository.save(any(MonthlyBudgetConfirmation.class)))
                .willReturn(savedConfirmation);

        // when
        MonthlyBudgetConfirmServiceResponse response = monthlyBudgetConfirmService
                .confirmMonthlyBudget(memberId, year, month, action);

        // then
        assertThat(response).isNotNull();
        assertThat(response.year()).isEqualTo(year);
        assertThat(response.month()).isEqualTo(month);
        assertThat(response.monthlyBudget()).isEqualTo(monthlyBudgetAmount);
        assertThat(response.confirmedAt()).isNotNull();

        then(confirmationRepository).should(times(1))
                .existsByMemberIdAndYearAndMonth(memberId, year, month);
        then(monthlyBudgetRepository).should(times(1))
                .findByMemberIdAndBudgetMonth(memberId, budgetMonth);
        then(confirmationRepository).should(times(1))
                .save(any(MonthlyBudgetConfirmation.class));
    }

    @Test
    @DisplayName("월별 예산 확인 - 성공 (CHANGE 액션)")
    void confirmMonthlyBudget_Success_ChangeAction() {
        // given
        Long memberId = 2L;
        Integer year = 2024;
        Integer month = 4;
        String action = "change";
        String budgetMonth = "2024-04";
        Integer monthlyBudgetAmount = 600000;

        given(confirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month))
                .willReturn(false);

        MonthlyBudget monthlyBudget = MonthlyBudget.reconstitute(2L, memberId, monthlyBudgetAmount, 0, budgetMonth);
        given(monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth))
                .willReturn(Optional.of(monthlyBudget));

        MonthlyBudgetConfirmation savedConfirmation = MonthlyBudgetConfirmation.reconstitute(
                2L, memberId, year, month, BudgetConfirmAction.CHANGE, LocalDateTime.now());
        given(confirmationRepository.save(any(MonthlyBudgetConfirmation.class)))
                .willReturn(savedConfirmation);

        // when
        MonthlyBudgetConfirmServiceResponse response = monthlyBudgetConfirmService
                .confirmMonthlyBudget(memberId, year, month, action);

        // then
        assertThat(response).isNotNull();
        assertThat(response.year()).isEqualTo(year);
        assertThat(response.month()).isEqualTo(month);
        assertThat(response.monthlyBudget()).isEqualTo(monthlyBudgetAmount);
        assertThat(response.confirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("월별 예산 확인 - 실패 (이미 확인된 월)")
    void confirmMonthlyBudget_Fail_AlreadyConfirmed() {
        // given
        Long memberId = 1L;
        Integer year = 2024;
        Integer month = 3;
        String action = "KEEP";

        given(confirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> monthlyBudgetConfirmService
                .confirmMonthlyBudget(memberId, year, month, action))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorType.ALREADY_CONFIRMED_MONTHLY_BUDGET.getMessage());

        then(confirmationRepository).should(times(1))
                .existsByMemberIdAndYearAndMonth(memberId, year, month);
        then(monthlyBudgetRepository).should(never())
                .findByMemberIdAndBudgetMonth(anyLong(), anyString());
        then(confirmationRepository).should(never())
                .save(any(MonthlyBudgetConfirmation.class));
    }

    @Test
    @DisplayName("월별 예산 확인 - 실패 (월별 예산 없음)")
    void confirmMonthlyBudget_Fail_MonthlyBudgetNotFound() {
        // given
        Long memberId = 1L;
        Integer year = 2024;
        Integer month = 5;
        String action = "KEEP";
        String budgetMonth = "2024-05";

        given(confirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month))
                .willReturn(false);

        given(monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> monthlyBudgetConfirmService
                .confirmMonthlyBudget(memberId, year, month, action))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ErrorType.MONTHLY_BUDGET_NOT_FOUND.getMessage());

        then(confirmationRepository).should(times(1))
                .existsByMemberIdAndYearAndMonth(memberId, year, month);
        then(monthlyBudgetRepository).should(times(1))
                .findByMemberIdAndBudgetMonth(memberId, budgetMonth);
        then(confirmationRepository).should(never())
                .save(any(MonthlyBudgetConfirmation.class));
    }

    @Test
    @DisplayName("월별 예산 확인 - 실패 (잘못된 액션)")
    void confirmMonthlyBudget_Fail_InvalidAction() {
        // given
        Long memberId = 1L;
        Integer year = 2024;
        Integer month = 3;
        String action = "INVALID_ACTION";
        String budgetMonth = "2024-03";

        given(confirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month))
                .willReturn(false);

        MonthlyBudget monthlyBudget = MonthlyBudget.reconstitute(1L, memberId, 500000, 0, budgetMonth);
        given(monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth))
                .willReturn(Optional.of(monthlyBudget));

        // when & then
        assertThatThrownBy(() -> monthlyBudgetConfirmService
                .confirmMonthlyBudget(memberId, year, month, action))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorType.INVALID_BUDGET_CONFIRM_ACTION.getMessage());

        then(confirmationRepository).should(times(1))
                .existsByMemberIdAndYearAndMonth(memberId, year, month);
        then(monthlyBudgetRepository).should(times(1))
                .findByMemberIdAndBudgetMonth(memberId, budgetMonth);
        then(confirmationRepository).should(never())
                .save(any(MonthlyBudgetConfirmation.class));
    }

    @Test
    @DisplayName("월별 예산 확인 - 경계값 테스트 (1월)")
    void confirmMonthlyBudget_EdgeCase_January() {
        // given
        Long memberId = 1L;
        Integer year = 2024;
        Integer month = 1;
        String action = "KEEP";
        String budgetMonth = "2024-01";
        Integer monthlyBudgetAmount = 500000;

        given(confirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month))
                .willReturn(false);

        MonthlyBudget monthlyBudget = MonthlyBudget.reconstitute(1L, memberId, monthlyBudgetAmount, 0, budgetMonth);
        given(monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth))
                .willReturn(Optional.of(monthlyBudget));

        MonthlyBudgetConfirmation savedConfirmation = MonthlyBudgetConfirmation.reconstitute(
                1L, memberId, year, month, BudgetConfirmAction.KEEP, LocalDateTime.now());
        given(confirmationRepository.save(any(MonthlyBudgetConfirmation.class)))
                .willReturn(savedConfirmation);

        // when
        MonthlyBudgetConfirmServiceResponse response = monthlyBudgetConfirmService
                .confirmMonthlyBudget(memberId, year, month, action);

        // then
        assertThat(response).isNotNull();
        assertThat(response.month()).isEqualTo(1);
    }

    @Test
    @DisplayName("월별 예산 확인 - 경계값 테스트 (12월)")
    void confirmMonthlyBudget_EdgeCase_December() {
        // given
        Long memberId = 1L;
        Integer year = 2024;
        Integer month = 12;
        String action = "KEEP";
        String budgetMonth = "2024-12";
        Integer monthlyBudgetAmount = 500000;

        given(confirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month))
                .willReturn(false);

        MonthlyBudget monthlyBudget = MonthlyBudget.reconstitute(1L, memberId, monthlyBudgetAmount, 0, budgetMonth);
        given(monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth))
                .willReturn(Optional.of(monthlyBudget));

        MonthlyBudgetConfirmation savedConfirmation = MonthlyBudgetConfirmation.reconstitute(
                1L, memberId, year, month, BudgetConfirmAction.KEEP, LocalDateTime.now());
        given(confirmationRepository.save(any(MonthlyBudgetConfirmation.class)))
                .willReturn(savedConfirmation);

        // when
        MonthlyBudgetConfirmServiceResponse response = monthlyBudgetConfirmService
                .confirmMonthlyBudget(memberId, year, month, action);

        // then
        assertThat(response).isNotNull();
        assertThat(response.month()).isEqualTo(12);
    }

    @Test
    @DisplayName("월별 예산 확인 - 경계값 테스트 (0원 예산)")
    void confirmMonthlyBudget_EdgeCase_ZeroBudget() {
        // given
        Long memberId = 1L;
        Integer year = 2024;
        Integer month = 3;
        String action = "KEEP";
        String budgetMonth = "2024-03";
        Integer monthlyBudgetAmount = 0;

        given(confirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month))
                .willReturn(false);

        MonthlyBudget monthlyBudget = MonthlyBudget.reconstitute(1L, memberId, monthlyBudgetAmount, 0, budgetMonth);
        given(monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth))
                .willReturn(Optional.of(monthlyBudget));

        MonthlyBudgetConfirmation savedConfirmation = MonthlyBudgetConfirmation.reconstitute(
                1L, memberId, year, month, BudgetConfirmAction.KEEP, LocalDateTime.now());
        given(confirmationRepository.save(any(MonthlyBudgetConfirmation.class)))
                .willReturn(savedConfirmation);

        // when
        MonthlyBudgetConfirmServiceResponse response = monthlyBudgetConfirmService
                .confirmMonthlyBudget(memberId, year, month, action);

        // then
        assertThat(response).isNotNull();
        assertThat(response.monthlyBudget()).isEqualTo(0);
    }

    @Test
    @DisplayName("월별 예산 확인 - 경계값 테스트 (매우 큰 금액)")
    void confirmMonthlyBudget_EdgeCase_LargeAmount() {
        // given
        Long memberId = 1L;
        Integer year = 2024;
        Integer month = 3;
        String action = "KEEP";
        String budgetMonth = "2024-03";
        Integer monthlyBudgetAmount = 999999999;

        given(confirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month))
                .willReturn(false);

        MonthlyBudget monthlyBudget = MonthlyBudget.reconstitute(1L, memberId, monthlyBudgetAmount, 0, budgetMonth);
        given(monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth))
                .willReturn(Optional.of(monthlyBudget));

        MonthlyBudgetConfirmation savedConfirmation = MonthlyBudgetConfirmation.reconstitute(
                1L, memberId, year, month, BudgetConfirmAction.KEEP, LocalDateTime.now());
        given(confirmationRepository.save(any(MonthlyBudgetConfirmation.class)))
                .willReturn(savedConfirmation);

        // when
        MonthlyBudgetConfirmServiceResponse response = monthlyBudgetConfirmService
                .confirmMonthlyBudget(memberId, year, month, action);

        // then
        assertThat(response).isNotNull();
        assertThat(response.monthlyBudget()).isEqualTo(monthlyBudgetAmount);
    }
}
