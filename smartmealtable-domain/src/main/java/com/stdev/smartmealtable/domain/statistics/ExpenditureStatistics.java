package com.stdev.smartmealtable.domain.statistics;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 지출 통계 정보
 * POJO record 타입 - Spring Data 의존성 없음
 */
public record ExpenditureStatistics(
        BigDecimal totalAmount,
        BigDecimal averageAmount,
        long totalCount,
        Map<String, BigDecimal> topCategoriesByAmount,  // categoryName -> amount
        Map<String, Long> expendituresByMealType,       // BREAKFAST, LUNCH, DINNER, SNACK
        BigDecimal averageAmountPerMember
) {
}
