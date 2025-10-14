package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.api.home.service.dto.HomeDashboardServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.ResourceNotFoundException;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.budget.MealBudgetRepository;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HomeDashboardQueryService {

    private final AddressHistoryRepository addressHistoryRepository;
    private final DailyBudgetRepository dailyBudgetRepository;
    private final MealBudgetRepository mealBudgetRepository;
    private final ExpenditureRepository expenditureRepository;

    public HomeDashboardServiceResponse getHomeDashboard(Long memberId) {
        AddressHistory primaryAddress = addressHistoryRepository.findPrimaryByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorType.ADDRESS_NOT_FOUND));

        LocalDate today = LocalDate.now();

        DailyBudget dailyBudget = dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today)
                .orElse(null);

        List<MealBudget> mealBudgets = mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today);

        Long todaySpentLong = expenditureRepository.getTotalAmountByPeriod(memberId, today, today);
        BigDecimal todaySpent = todaySpentLong != null ? BigDecimal.valueOf(todaySpentLong) : BigDecimal.ZERO;

        BigDecimal breakfastSpent = BigDecimal.ZERO;
        BigDecimal lunchSpent = BigDecimal.ZERO;
        BigDecimal dinnerSpent = BigDecimal.ZERO;
        BigDecimal otherSpent = BigDecimal.ZERO;

        BigDecimal todayBudget = dailyBudget != null ? BigDecimal.valueOf(dailyBudget.getDailyFoodBudget()) : BigDecimal.ZERO;

        return HomeDashboardServiceResponse.of(
                primaryAddress,
                todayBudget,
                todaySpent,
                mealBudgets,
                breakfastSpent,
                lunchSpent,
                dinnerSpent,
                otherSpent
        );
    }
}
