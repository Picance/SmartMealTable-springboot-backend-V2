package com.stdev.smartmealtable.api.expenditure.dto.response;

import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureDetailServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureItemServiceResponse;
import com.stdev.smartmealtable.domain.expenditure.MealType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 내역 상세 조회 응답 DTO
 */
public record GetExpenditureDetailResponse(
        Long expenditureId,
        String storeName,
        Integer amount,
        LocalDate expendedDate,
        LocalTime expendedTime,
        Long categoryId,
        String categoryName,
        MealType mealType,
        String memo,
        List<ExpenditureItemResponse> items
) {
    
    public static GetExpenditureDetailResponse from(ExpenditureDetailServiceResponse serviceResponse) {
        List<ExpenditureItemResponse> items = serviceResponse.getItems() != null
                ? serviceResponse.getItems().stream()
                .map(ExpenditureItemResponse::from)
                .collect(Collectors.toList())
                : List.of();
        
        return new GetExpenditureDetailResponse(
                serviceResponse.getExpenditureId(),
                serviceResponse.getStoreName(),
                serviceResponse.getAmount(),
                serviceResponse.getExpendedDate(),
                serviceResponse.getExpendedTime(),
                serviceResponse.getCategoryId(),
                serviceResponse.getCategoryName(),
                serviceResponse.getMealType(),
                serviceResponse.getMemo(),
                items
        );
    }
    
    public record ExpenditureItemResponse(
            Long expenditureItemId,
            Long foodId,
            String foodName,
            Integer quantity,
            Integer price
    ) {
        public static ExpenditureItemResponse from(ExpenditureItemServiceResponse serviceResponse) {
            return new ExpenditureItemResponse(
                    serviceResponse.getExpenditureItemId(),
                    serviceResponse.getFoodId(),
                    serviceResponse.getFoodName(),
                    serviceResponse.getQuantity(),
                    serviceResponse.getPrice()
            );
        }
    }
}
