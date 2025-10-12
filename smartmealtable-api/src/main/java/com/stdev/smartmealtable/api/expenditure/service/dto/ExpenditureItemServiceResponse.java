package com.stdev.smartmealtable.api.expenditure.service.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 지출 항목 응답 DTO
 */
@Getter
@Builder
public class ExpenditureItemServiceResponse {
    
    private Long expenditureItemId;
    private Long foodId;
    private String foodName;
    private Integer quantity;
    private Integer price;
}
