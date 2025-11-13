package com.stdev.smartmealtable.api.budget.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 월별 예산 등록 Application Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateMonthlyBudgetService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;

    /**
     * 월별 예산 등록
     */
    public CreateMonthlyBudgetServiceResponse createMonthlyBudget(Long memberId, CreateMonthlyBudgetServiceRequest request) {
        monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, request.getBudgetMonth())
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorType.MONTHLY_BUDGET_ALREADY_EXISTS);
                });

        MonthlyBudget monthlyBudget = MonthlyBudget.create(
                memberId,
                request.getMonthlyFoodBudget(),
                request.getBudgetMonth()
        );

        MonthlyBudget saved = monthlyBudgetRepository.save(monthlyBudget);
        return new CreateMonthlyBudgetServiceResponse(
                saved.getMonthlyBudgetId(),
                saved.getMonthlyFoodBudget(),
                saved.getBudgetMonth()
        );
    }
}
