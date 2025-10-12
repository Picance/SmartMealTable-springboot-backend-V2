package com.stdev.smartmealtable.api.expenditure.service.dto;

/**
 * 지출 내역 요약 정보 Service Response DTO
 */
public record ExpenditureSummaryServiceResponse(
        Integer totalAmount,
        Integer totalCount,
        Integer averageAmount
) {
}
