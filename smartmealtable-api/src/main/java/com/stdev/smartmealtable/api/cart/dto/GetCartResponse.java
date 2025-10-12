package com.stdev.smartmealtable.api.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 장바구니 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetCartResponse {
    
    /**
     * 장바구니 ID
     */
    private Long cartId;
    
    /**
     * 가게 ID
     */
    private Long storeId;
    
    /**
     * 가게 이름
     */
    private String storeName;
    
    /**
     * 장바구니 아이템 목록
     */
    private List<CartItemResponse> items;
    
    /**
     * 총 아이템 개수 (수량의 합)
     */
    private int totalQuantity;
    
    /**
     * 총 금액
     */
    private int totalAmount;
}
