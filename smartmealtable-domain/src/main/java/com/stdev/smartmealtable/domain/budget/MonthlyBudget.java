package com.stdev.smartmealtable.domain.budget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 월별 예산 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyBudget {
    private Long monthlyBudgetId;
    private Long memberId;
    private Integer monthlyFoodBudget;
    private Integer monthlyUsedAmount;
    private String budgetMonth;

    /**
     * 월별 예산 생성 팩토리 메서드
     */
    public static MonthlyBudget create(Long memberId, Integer monthlyFoodBudget, String budgetMonth) {
        MonthlyBudget budget = new MonthlyBudget();
        budget.memberId = memberId;
        budget.monthlyFoodBudget = monthlyFoodBudget;
        budget.monthlyUsedAmount = 0;
        budget.budgetMonth = budgetMonth;
        return budget;
    }

    /**
     * 영속화된 월별 예산 재구성 (JPA Entity → Domain 변환용)
     */
    public static MonthlyBudget reconstitute(Long monthlyBudgetId, Long memberId, Integer monthlyFoodBudget, 
                                              Integer monthlyUsedAmount, String budgetMonth) {
        MonthlyBudget budget = new MonthlyBudget();
        budget.monthlyBudgetId = monthlyBudgetId;
        budget.memberId = memberId;
        budget.monthlyFoodBudget = monthlyFoodBudget;
        budget.monthlyUsedAmount = monthlyUsedAmount;
        budget.budgetMonth = budgetMonth;
        return budget;
    }

    /**
     * 월별 예산 금액 변경
     */
    public void changeMonthlyFoodBudget(Integer newBudget) {
        this.monthlyFoodBudget = newBudget;
    }

    /**
     * 사용 금액 추가
     */
    public void addUsedAmount(Integer amount) {
        this.monthlyUsedAmount += amount;
    }
}
