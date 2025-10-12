package com.stdev.smartmealtable.api.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니 아이템 추가 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemRequest {
    
    /**
     * 가게 ID
     */
    private Long storeId;
    
    /**
     * 음식 ID
     */
    private Long foodId;
    
    /**
     * 수량
     */
    private Integer quantity;
}
