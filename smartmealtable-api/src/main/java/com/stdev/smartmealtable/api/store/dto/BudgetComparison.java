package com.stdev.smartmealtable.api.store.dto;

/**
 * 음식 가격과 사용자 예산 비교 정보 DTO
 */
public record BudgetComparison(
        Integer userMealBudget,
        Integer foodPrice,
        Integer difference,
        Boolean isOverBudget,
        String differenceText
) {
    public static BudgetComparison of(Integer userMealBudget, Integer foodPrice) {
        if (userMealBudget == null || foodPrice == null) {
            return new BudgetComparison(userMealBudget, foodPrice, null, false, null);
        }
        
        int difference = userMealBudget - foodPrice;
        boolean isOverBudget = difference < 0;
        String differenceText = String.format("%s%,d원", isOverBudget ? "+" : "-", Math.abs(difference));
        
        return new BudgetComparison(userMealBudget, foodPrice, difference, isOverBudget, differenceText);
    }
}
