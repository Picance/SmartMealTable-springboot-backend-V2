package com.stdev.smartmealtable.api.expenditure.dto.response;

import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureStatisticsServiceResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 통계 조회 응답 Response DTO
 * 캘린더 뷰용 일별 지출 및 예산 비교 정보
 */
public record GetExpenditureStatisticsResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<DailyStatistics> dailyStatistics
) {
    public record DailyStatistics(
            LocalDate date,
            Long totalSpentAmount,
            Long budget,
            Long balance,
            Boolean overBudget,
            Boolean budgetRegistered
    ) {
    }

    public static GetExpenditureStatisticsResponse from(
            LocalDate startDate,
            LocalDate endDate,
            ExpenditureStatisticsServiceResponse serviceResponse
    ) {
        List<DailyStatistics> dailyStatsList = serviceResponse.dailyStatistics().stream()
                .map(stat -> new DailyStatistics(
                        stat.date(),
                        stat.totalSpentAmount(),
                        stat.budget(),
                        stat.balance(),
                        stat.overBudget(),
                        stat.budgetRegistered()
                ))
                .collect(Collectors.toList());

        return new GetExpenditureStatisticsResponse(
                startDate,
                endDate,
                dailyStatsList
        );
    }
}
