package com.stdev.smartmealtable.api.expenditure.service.dto;

import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 지출 내역 상세 조회 응답 DTO
 */
@Getter
@Builder
public class ExpenditureDetailServiceResponse {
    
    private Long expenditureId;
    private String storeName;
    private Integer amount;
    private LocalDate expendedDate;
    private LocalTime expendedTime;
    private Long categoryId;
    private String categoryName;
    private MealType mealType;
    private String memo;
    private List<ExpenditureItemServiceResponse> items;
}
