package com.stdev.smartmealtable.api.expenditure.dto.request;

import com.stdev.smartmealtable.domain.expenditure.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 지출 내역 수정 요청 DTO
 */
public record UpdateExpenditureRequest(
        @NotBlank(message = "가게 이름은 필수입니다.")
        @Size(max = 200, message = "가게 이름은 200자를 초과할 수 없습니다.")
        String storeName,
        
        @NotNull(message = "금액은 필수입니다.")
        @Min(value = 0, message = "금액은 0 이상이어야 합니다.")
        Integer amount,
        
        @NotNull(message = "지출 날짜는 필수입니다.")
        LocalDate expendedDate,
        
        LocalTime expendedTime,
        
        Long categoryId,
        
        MealType mealType,
        
        @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다.")
        String memo,
        
        @Valid
        List<ExpenditureItemRequest> items
) {
    public record ExpenditureItemRequest(
            @NotNull(message = "음식 ID는 필수입니다.")
            Long foodId,
            
            @NotNull(message = "수량은 필수입니다.")
            @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
            Integer quantity,
            
            @NotNull(message = "가격은 필수입니다.")
            @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
            Integer price
    ) {}
}
