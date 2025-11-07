package com.stdev.smartmealtable.api.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 장바구니 체크아웃 (지출 등록) 응답 DTO
 */
@Builder
public record CartCheckoutResponse(
        @JsonProperty("expenditureId")
        Long expenditureId,
        
        @JsonProperty("storeName")
        String storeName,
        
        @JsonProperty("items")
        List<CheckoutItemResponse> items,
        
        @JsonProperty("subtotal")
        Long subtotal,
        
        @JsonProperty("discount")
        Long discount,
        
        @JsonProperty("finalAmount")
        Long finalAmount,
        
        @JsonProperty("mealType")
        String mealType,
        
        @JsonProperty("expendedDate")
        LocalDate expendedDate,
        
        @JsonProperty("expendedTime")
        LocalTime expendedTime,
        
        @JsonProperty("budgetSummary")
        BudgetSummary budgetSummary,
        
        @JsonProperty("createdAt")
        LocalDateTime createdAt
) {
    @Builder
    public record CheckoutItemResponse(
            @JsonProperty("foodName")
            String foodName,
            
            @JsonProperty("quantity")
            Integer quantity,
            
            @JsonProperty("price")
            Long price
    ) {}
    
    @Builder
    public record BudgetSummary(
            @JsonProperty("mealBudgetBefore")
            Long mealBudgetBefore,
            
            @JsonProperty("mealBudgetAfter")
            Long mealBudgetAfter,
            
            @JsonProperty("dailyBudgetBefore")
            Long dailyBudgetBefore,
            
            @JsonProperty("dailyBudgetAfter")
            Long dailyBudgetAfter,
            
            @JsonProperty("monthlyBudgetBefore")
            Long monthlyBudgetBefore,
            
            @JsonProperty("monthlyBudgetAfter")
            Long monthlyBudgetAfter
    ) {}
}
