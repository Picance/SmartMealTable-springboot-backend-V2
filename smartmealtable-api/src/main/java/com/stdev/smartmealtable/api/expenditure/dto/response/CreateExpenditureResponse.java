package com.stdev.smartmealtable.api.expenditure.dto.response;

import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureItemServiceResponse;
import com.stdev.smartmealtable.domain.expenditure.MealType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 내역 등록 응답 DTO
 */
public record CreateExpenditureResponse(
        Long expenditureId,
        String storeName,
        Integer amount,
        LocalDate expendedDate,
        LocalTime expendedTime,
        Long categoryId,
        String categoryName,
        MealType mealType,
        String memo,
        List<ExpenditureItemResponse> items,
        LocalDateTime createdAt
) {
    public static CreateExpenditureResponse from(CreateExpenditureServiceResponse serviceResponse) {
        List<ExpenditureItemResponse> itemResponses = null;
        
        if (serviceResponse.items() != null) {
            itemResponses = serviceResponse.items().stream()
                    .map(ExpenditureItemResponse::from)
                    .collect(Collectors.toList());
        }
        
        return new CreateExpenditureResponse(
                serviceResponse.expenditureId(),
                serviceResponse.storeName(),
                serviceResponse.amount(),
                serviceResponse.expendedDate(),
                serviceResponse.expendedTime(),
                serviceResponse.categoryId(),
                serviceResponse.categoryName(),
                serviceResponse.mealType(),
                serviceResponse.memo(),
                itemResponses,
                serviceResponse.createdAt()
        );
    }
    
    /**
     * 지출 항목 응답 DTO
     */
    public record ExpenditureItemResponse(
            Long expenditureItemId,
            Long foodId,
            String foodName,
            Integer quantity,
            Integer price
    ) {
        public static ExpenditureItemResponse from(ExpenditureItemServiceResponse serviceItem) {
            return new ExpenditureItemResponse(
                    serviceItem.getExpenditureItemId(),
                    serviceItem.getFoodId(),
                    serviceItem.getFoodName(),
                    serviceItem.getQuantity(),
                    serviceItem.getPrice()
            );
        }
    }
}
