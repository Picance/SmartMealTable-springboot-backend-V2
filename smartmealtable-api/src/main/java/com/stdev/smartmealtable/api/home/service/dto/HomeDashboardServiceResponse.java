package com.stdev.smartmealtable.api.home.service.dto;

import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Builder
public record HomeDashboardServiceResponse(
        LocationInfo location,
        BudgetInfo budget,
        List<RecommendedMenuInfo> recommendedMenus,
        List<RecommendedStoreInfo> recommendedStores
) {

    public static HomeDashboardServiceResponse of(
            AddressHistory primaryAddress,
            BigDecimal todayBudget,
            BigDecimal todaySpent,
            List<MealBudget> mealBudgets,
            BigDecimal breakfastSpent,
            BigDecimal lunchSpent,
            BigDecimal dinnerSpent,
            BigDecimal otherSpent
    ) {
        Address address = primaryAddress.getAddress();

        LocationInfo locationInfo = new LocationInfo(
                primaryAddress.getAddressHistoryId(),
                address.getAlias(),
                address.getFullAddress(),
                address.getStreetNameAddress(),
                address.getLatitude(),
                address.getLongitude(),
                primaryAddress.getIsPrimary()
        );

        List<MealBudgetInfo> mealBudgetInfos = buildMealBudgetInfos(
                mealBudgets,
                breakfastSpent,
                lunchSpent,
                dinnerSpent,
                otherSpent
        );

        BigDecimal remaining = todayBudget.subtract(todaySpent);
        int utilizationRate = calculateUtilizationRate(todayBudget, todaySpent);

        BudgetInfo budgetInfo = new BudgetInfo(
                todayBudget.intValue(),
                todaySpent.intValue(),
                remaining.intValue(),
                utilizationRate,
                mealBudgetInfos
        );

        return new HomeDashboardServiceResponse(locationInfo, budgetInfo, List.of(), List.of());
    }

    private static List<MealBudgetInfo> buildMealBudgetInfos(
            List<MealBudget> mealBudgets,
            BigDecimal breakfastSpent,
            BigDecimal lunchSpent,
            BigDecimal dinnerSpent,
            BigDecimal otherSpent
    ) {
        return mealBudgets.stream()
                .map(mb -> {
                    Integer budget = mb.getMealBudget();
                    BigDecimal spent = switch (mb.getMealType()) {
                        case BREAKFAST -> breakfastSpent;
                        case LUNCH -> lunchSpent;
                        case DINNER -> dinnerSpent;
                        default -> otherSpent;
                    };

                    Integer mealRemaining = budget - spent.intValue();

                    return new MealBudgetInfo(
                            mb.getMealType().name(),
                            budget,
                            spent.intValue(),
                            mealRemaining
                    );
                })
                .toList();
    }

    private static int calculateUtilizationRate(BigDecimal budget, BigDecimal spent) {
        if (budget.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return spent.multiply(BigDecimal.valueOf(100))
                .divide(budget, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    public record LocationInfo(
            Long addressHistoryId,
            String addressAlias,
            String fullAddress,
            String roadAddress,
            Double latitude,
            Double longitude,
            Boolean isPrimary
    ) {}

    public record BudgetInfo(
            Integer todayBudget,
            Integer todaySpent,
            Integer todayRemaining,
            Integer utilizationRate,
            List<MealBudgetInfo> mealBudgets
    ) {}

    public record MealBudgetInfo(
            String mealType,
            Integer budget,
            Integer spent,
            Integer remaining
    ) {}

    public record RecommendedMenuInfo(
            Long foodId,
            String foodName,
            String storeName,
            Integer averagePrice,
            BigDecimal distance,
            String distanceText,
            List<String> tags
    ) {}

    public record RecommendedStoreInfo(
            Long storeId,
            String storeName,
            String categoryName,
            Integer averageFoodPrice,
            BigDecimal distance,
            String distanceText,
            String businessHours,
            String contextInfo
    ) {}
}
