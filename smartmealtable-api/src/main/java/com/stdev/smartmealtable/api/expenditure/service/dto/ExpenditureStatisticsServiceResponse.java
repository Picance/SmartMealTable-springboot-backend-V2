package com.stdev.smartmealtable.api.expenditure.service.dto;

import com.stdev.smartmealtable.domain.expenditure.MealType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 지출 통계 조회 응답 Service DTO
 */
public record ExpenditureStatisticsServiceResponse(
        Long totalAmount,
        Map<Long, CategoryStatistics> categoryStatistics,
        List<DailyStatistics> dailyStatistics,
        Map<MealType, Long> mealTypeStatistics
) {
    public record CategoryStatistics(
            Long categoryId,
            String categoryName,
            Long amount
    ) {
    }

    public record DailyStatistics(
            LocalDate date,
            Long totalSpentAmount,
            Long budget,
            Long balance,
            Boolean overBudget
    ) {
    }
}
