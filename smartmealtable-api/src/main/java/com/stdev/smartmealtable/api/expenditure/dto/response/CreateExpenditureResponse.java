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
        Long storeId,                       // ◆ 새로 추가
        Boolean hasStoreLink,               // ◆ 새로 추가 (storeId 존재 여부)
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
                serviceResponse.storeId(),                      // ◆ record의 필드 직접 접근
                serviceResponse.storeId() != null,             // ◆ storeId 존재 여부
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
            Boolean hasFoodLink,               // ◆ 새로 추가 (foodId 존재 여부)
            String foodName,
            Integer quantity,
            Integer price
    ) {
        public static ExpenditureItemResponse from(ExpenditureItemServiceResponse serviceItem) {
            return new ExpenditureItemResponse(
                    serviceItem.getExpenditureItemId(),
                    serviceItem.getFoodId(),
                    serviceItem.getFoodId() != null,            // ◆ 추가: foodId 존재 여부
                    serviceItem.getFoodName(),
                    serviceItem.getQuantity(),
                    serviceItem.getPrice()
            );
        }
    }
}
