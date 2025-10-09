package com.stdev.smartmealtable.domain.budget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 일일 예산 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyBudget {
    private Long budgetId;
    private Long memberId;
    private Integer dailyFoodBudget;
    private Integer dailyUsedAmount;
    private LocalDate budgetDate;

    /**
     * 일일 예산 생성 팩토리 메서드
     */
    public static DailyBudget create(Long memberId, Integer dailyFoodBudget, LocalDate budgetDate) {
        DailyBudget budget = new DailyBudget();
        budget.memberId = memberId;
        budget.dailyFoodBudget = dailyFoodBudget;
        budget.dailyUsedAmount = 0;
        budget.budgetDate = budgetDate;
        return budget;
    }

    /**
     * 영속화된 일일 예산 재구성 (JPA Entity → Domain 변환용)
     */
    public static DailyBudget reconstitute(Long budgetId, Long memberId, Integer dailyFoodBudget,
                                           Integer dailyUsedAmount, LocalDate budgetDate) {
        DailyBudget budget = new DailyBudget();
        budget.budgetId = budgetId;
        budget.memberId = memberId;
        budget.dailyFoodBudget = dailyFoodBudget;
        budget.dailyUsedAmount = dailyUsedAmount;
        budget.budgetDate = budgetDate;
        return budget;
    }

    /**
     * 일일 예산 금액 변경
     */
    public void changeDailyFoodBudget(Integer newBudget) {
        this.dailyFoodBudget = newBudget;
    }

    /**
     * 사용 금액 추가
     */
    public void addUsedAmount(Integer amount) {
        this.dailyUsedAmount += amount;
    }
}
