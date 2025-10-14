package com.stdev.smartmealtable.domain.budget;

import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 식사 예산 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealBudget {
    private Long mealBudgetId;
    private Long dailyBudgetId;
    private Integer mealBudget;
    private MealType mealType;
    private Integer usedAmount;
    private LocalDate budgetDate;

    /**
     * 식사 예산 생성 팩토리 메서드
     */
    public static MealBudget create(Long dailyBudgetId, Integer mealBudget, MealType mealType, LocalDate budgetDate) {
        MealBudget budget = new MealBudget();
        budget.dailyBudgetId = dailyBudgetId;
        budget.mealBudget = mealBudget;
        budget.mealType = mealType;
        budget.usedAmount = 0;
        budget.budgetDate = budgetDate;
        return budget;
    }

    /**
     * 영속화된 식사 예산 재구성 (JPA Entity → Domain 변환용)
     */
    public static MealBudget reconstitute(Long mealBudgetId, Long dailyBudgetId, Integer mealBudget,
                                          MealType mealType, Integer usedAmount, LocalDate budgetDate) {
        MealBudget budget = new MealBudget();
        budget.mealBudgetId = mealBudgetId;
        budget.dailyBudgetId = dailyBudgetId;
        budget.mealBudget = mealBudget;
        budget.mealType = mealType;
        budget.usedAmount = usedAmount;
        budget.budgetDate = budgetDate;
        return budget;
    }

    /**
     * 식사 예산 금액 변경
     */
    public void changeMealBudget(Integer newBudget) {
        this.mealBudget = newBudget;
    }

    /**
     * 사용 금액 추가
     */
    public void addUsedAmount(Integer amount) {
        this.usedAmount += amount;
    }
}
