package com.stdev.smartmealtable.api.budget.service;

import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.budget.service.BudgetDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 일일 예산 일괄 등록 Application Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BulkCreateDailyBudgetService {

    private final BudgetDomainService budgetDomainService;

    public BulkCreateDailyBudgetServiceResponse createDailyBudgets(Long memberId, BulkCreateDailyBudgetServiceRequest request) {
        BudgetDomainService.DailyBudgetBatchResult batchResult = budgetDomainService.createDailyBudgetsInRange(
                memberId,
                request.getStartDate(),
                request.getEndDate(),
                request.getDailyFoodBudget(),
                request.getMealBudgets()
        );

        List<MealBudget> mealBudgets = batchResult.mealBudgets().stream()
                .filter(mb -> mb.getBudgetDate().equals(request.getStartDate()))
                .toList();

        List<BulkCreateDailyBudgetServiceResponse.MealBudgetInfo> mealBudgetInfos = mealBudgets.stream()
                .map(mb -> new BulkCreateDailyBudgetServiceResponse.MealBudgetInfo(mb.getMealType(), mb.getMealBudget()))
                .toList();

        String message = String.format("%s부터 %s까지 일일 예산 %d건이 생성되었습니다.",
                formatDate(request.getStartDate()),
                formatDate(request.getEndDate()),
                batchResult.dailyBudgets().size());

        return new BulkCreateDailyBudgetServiceResponse(
                request.getStartDate(),
                request.getEndDate(),
                batchResult.dailyBudgets().size(),
                request.getDailyFoodBudget(),
                mealBudgetInfos,
                message
        );
    }

    private String formatDate(LocalDate date) {
        return date.toString();
    }
}
