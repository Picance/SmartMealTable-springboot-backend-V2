package com.stdev.smartmealtable.api.expenditure.dto.response;

import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureStatisticsServiceResponse;
import com.stdev.smartmealtable.domain.expenditure.MealType;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 통계 조회 응답 Response DTO
 */
public record GetExpenditureStatisticsResponse(
        Long totalAmount,
        List<CategoryStatistics> categoryStatistics,
        List<DailyStatistics> dailyStatistics,
        List<MealTypeStatistics> mealTypeStatistics
) {
    public record CategoryStatistics(
            Long categoryId,
            String categoryName,
            Long amount
    ) {
    }
    
    public record DailyStatistics(
            LocalDate date,
            Long amount
    ) {
    }
    
    public record MealTypeStatistics(
            MealType mealType,
            Long amount
    ) {
    }
    
    public static GetExpenditureStatisticsResponse from(ExpenditureStatisticsServiceResponse serviceResponse) {
        List<CategoryStatistics> categoryStatsList = serviceResponse.categoryStatistics().values().stream()
                .map(stat -> new CategoryStatistics(stat.categoryId(), stat.categoryName(), stat.amount()))
                .collect(Collectors.toList());
        
        List<DailyStatistics> dailyStatsList = serviceResponse.dailyStatistics().entrySet().stream()
                .map(entry -> new DailyStatistics(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        List<MealTypeStatistics> mealTypeStatsList = serviceResponse.mealTypeStatistics().entrySet().stream()
                .map(entry -> new MealTypeStatistics(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        return new GetExpenditureStatisticsResponse(
                serviceResponse.totalAmount(),
                categoryStatsList,
                dailyStatsList,
                mealTypeStatsList
        );
    }
}
