package com.stdev.smartmealtable.admin.statistics.dto.response;

import com.stdev.smartmealtable.domain.statistics.ExpenditureStatistics;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 지출 통계 응답 DTO
 */
public record ExpenditureStatisticsResponse(
        BigDecimal totalAmount,
        BigDecimal averageAmount,
        long totalCount,
        Map<String, BigDecimal> topCategoriesByAmount,
        Map<String, Long> expendituresByMealType,
        BigDecimal averageAmountPerMember
) {
    public static ExpenditureStatisticsResponse from(ExpenditureStatistics statistics) {
        return new ExpenditureStatisticsResponse(
                statistics.totalAmount(),
                statistics.averageAmount(),
                statistics.totalCount(),
                statistics.topCategoriesByAmount(),
                statistics.expendituresByMealType(),
                statistics.averageAmountPerMember()
        );
    }
}
