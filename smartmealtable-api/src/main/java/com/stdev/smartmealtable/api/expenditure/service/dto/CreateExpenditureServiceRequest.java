package com.stdev.smartmealtable.api.expenditure.service.dto;

import com.stdev.smartmealtable.domain.expenditure.MealType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 지출 내역 등록 서비스 요청 DTO
 */
public record CreateExpenditureServiceRequest(
        Long memberId,
        Long storeId,                       // ◆ 새로 추가 (nullable)
        String storeName,
        Integer amount,
        LocalDate expendedDate,
        LocalTime expendedTime,
        Long categoryId,
        MealType mealType,
        String memo,
        List<ExpenditureItemServiceRequest> items
) {
    /**
     * 지출 항목 서비스 요청 DTO
     */
    public record ExpenditureItemServiceRequest(
            Long foodId,                    // ◆ nullable
            String foodName,                // ◆ 새로 추가
            Integer quantity,
            Integer price
    ) {}
}
