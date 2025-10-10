package com.stdev.smartmealtable.api.expenditure.service.dto;

import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.MealType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 내역 등록 서비스 응답 DTO
 */
public record CreateExpenditureServiceResponse(
        Long expenditureId,
        String storeName,
        Integer amount,
        LocalDate expendedDate,
        LocalTime expendedTime,
        Long categoryId,
        String categoryName,
        MealType mealType,
        String memo,
        List<ExpenditureItemServiceResponse> items,
        LocalDateTime createdAt
) {
    public static CreateExpenditureServiceResponse from(Expenditure expenditure, String categoryName) {
        List<ExpenditureItemServiceResponse> itemResponses = expenditure.getItems().stream()
                .map(ExpenditureItemServiceResponse::from)
                .collect(Collectors.toList());
        
        return new CreateExpenditureServiceResponse(
                expenditure.getExpenditureId(),
                expenditure.getStoreName(),
                expenditure.getAmount(),
                expenditure.getExpendedDate(),
                expenditure.getExpendedTime(),
                expenditure.getCategoryId(),
                categoryName,
                expenditure.getMealType(),
                expenditure.getMemo(),
                itemResponses,
                expenditure.getCreatedAt()
        );
    }
    
    /**
     * 지출 항목 서비스 응답 DTO
     */
    public record ExpenditureItemServiceResponse(
            Long expenditureItemId,
            String foodName,
            Integer quantity,
            Integer price
    ) {
        public static ExpenditureItemServiceResponse from(com.stdev.smartmealtable.domain.expenditure.ExpenditureItem item) {
            return new ExpenditureItemServiceResponse(
                    item.getExpenditureItemId(),
                    item.getFoodName(),
                    item.getQuantity(),
                    item.getPrice()
            );
        }
    }
}
