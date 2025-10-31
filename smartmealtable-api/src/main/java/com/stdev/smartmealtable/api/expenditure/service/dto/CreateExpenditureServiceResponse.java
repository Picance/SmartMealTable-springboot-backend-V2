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
        Long storeId,                       // ◆ 새로 추가
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
                .map(item -> ExpenditureItemServiceResponse.builder()
                        .expenditureItemId(item.getExpenditureItemId())
                        .foodId(item.getFoodId())
                        .foodName(item.getFoodName())   // ◆ 이제 foodName도 저장됨
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());
        
        return new CreateExpenditureServiceResponse(
                expenditure.getExpenditureId(),
                expenditure.getStoreId(),               // ◆ 추가
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
}
