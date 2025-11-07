package com.stdev.smartmealtable.api.cart.dto;

import com.stdev.smartmealtable.domain.expenditure.MealType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 장바구니 체크아웃 (지출 등록) 요청 DTO
 */
public record CartCheckoutRequest(
        @NotNull(message = "가게 ID는 필수입니다.")
        Long storeId,
        
        @NotNull(message = "식사 유형은 필수입니다.")
        MealType mealType,
        
        @Min(value = 0, message = "할인액은 0 이상이어야 합니다.")
        Long discount,
        
        @NotNull(message = "지출 날짜는 필수입니다.")
        LocalDate expendedDate,
        
        @NotNull(message = "지출 시간은 필수입니다.")
        LocalTime expendedTime,
        
        @Size(max = 500, message = "메모는 최대 500자입니다.")
        String memo
) {}
