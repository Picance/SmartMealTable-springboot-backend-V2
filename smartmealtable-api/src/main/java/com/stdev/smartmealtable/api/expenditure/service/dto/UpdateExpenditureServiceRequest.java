package com.stdev.smartmealtable.api.expenditure.service.dto;

import com.stdev.smartmealtable.domain.expenditure.MealType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 지출 내역 수정 서비스 요청 DTO
 */
public record UpdateExpenditureServiceRequest(
        String storeName,
        Integer amount,
        Long discount,
        LocalDate expendedDate,
        LocalTime expendedTime,
        Long categoryId,
        MealType mealType,
        String memo,
        List<ExpenditureItemRequest> items
) {
    public record ExpenditureItemRequest(
            Long foodId,
            Integer quantity,
            Integer price
    ) {}
}
