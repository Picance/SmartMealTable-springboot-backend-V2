package com.stdev.smartmealtable.api.expenditure.service.dto;

import com.stdev.smartmealtable.domain.expenditure.MealType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

/**
 * 지출 내역 목록 조회 Service Response DTO
 */
public record ExpenditureListServiceResponse(
        ExpenditureSummaryServiceResponse summary,
        Page<ExpenditureInfo> expenditures
) {
    
    /**
     * 개별 지출 내역 정보
     */
    public record ExpenditureInfo(
            Long expenditureId,
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            String categoryName,
            MealType mealType
    ) {
    }
}
