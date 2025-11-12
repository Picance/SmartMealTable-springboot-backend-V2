package com.stdev.smartmealtable.api.cart.controller;

import com.stdev.smartmealtable.api.cart.dto.*;
import com.stdev.smartmealtable.api.cart.service.CartService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 장바구니 Controller
 * 장바구니 조회, 아이템 추가/수정/삭제, 전체 삭제 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    /**
     * 장바구니 아이템 추가
     * POST /api/v1/cart/items
     * 
     * @param user 인증된 사용자 정보
     * @param request 장바구니 아이템 추가 요청
     * @return 추가된 아이템 정보
     */
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddCartItemResponse> addCartItem(
            @AuthUser AuthenticatedUser user,
            @RequestBody AddCartItemRequest request) {
        AddCartItemResponse response = cartService.addCartItem(user.memberId(), request);
        return ApiResponse.success(response);
    }
    
    /**
     * 특정 가게의 장바구니 조회
     * GET /api/v1/cart/store/{storeId}
     * 
     * @param user 인증된 사용자 정보
     * @param storeId 가게 ID
     * @return 장바구니 정보
     */
    @GetMapping("/store/{storeId}")
    public ApiResponse<GetCartResponse> getCart(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long storeId) {
        GetCartResponse response = cartService.getCart(user.memberId(), storeId);
        return ApiResponse.success(response);
    }
    
    /**
     * 내 모든 장바구니 조회
     * GET /api/v1/cart
     * 
     * @param user 인증된 사용자 정보
     * @return 장바구니 목록
     */
    @GetMapping
    public ApiResponse<List<GetCartResponse>> getAllCarts(@AuthUser AuthenticatedUser user) {
        List<GetCartResponse> response = cartService.getAllCarts(user.memberId());
        return ApiResponse.success(response);
    }
    
    /**
     * 장바구니 아이템 수량 수정
     * PUT /api/v1/cart/items/{cartItemId}
     * 
     * @param user 인증된 사용자 정보
     * @param cartItemId 장바구니 아이템 ID
     * @param request 수량 수정 요청
     * @return 성공 응답
     */
    @PutMapping("/items/{cartItemId}")
    public ApiResponse<Void> updateCartItemQuantity(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemQuantityRequest request) {
        cartService.updateCartItemQuantity(user.memberId(), cartItemId, request);
        return ApiResponse.success(null);
    }
    
    /**
     * 장바구니 아이템 삭제
     * DELETE /api/v1/cart/items/{cartItemId}
     * 
     * @param user 인증된 사용자 정보
     * @param cartItemId 장바구니 아이템 ID
     * @return 성공 응답
     */
    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<Void> removeCartItem(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long cartItemId) {
        cartService.removeCartItem(user.memberId(), cartItemId);
        return ApiResponse.success(null);
    }
    
    /**
     * 특정 가게 장바구니 삭제 (비우기)
     * DELETE /api/v1/cart/store/{storeId}
     *
     * @param user 인증된 사용자 정보
     * @param storeId 가게 ID
     * @return 성공 응답
     */
    @DeleteMapping("/store/{storeId}")
    public ApiResponse<Void> clearCart(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long storeId) {
        cartService.clearCart(user.memberId(), storeId);
        return ApiResponse.success(null);
    }

    /**
     * 장바구니 전체 비우기
     * DELETE /api/v1/cart
     *
     * @param user 인증된 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearAllCarts(@AuthUser AuthenticatedUser user) {
        cartService.clearAllCarts(user.memberId());
    }

    /**
     * 장바구니 체크아웃 (지출 등록)
     * POST /api/v1/cart/checkout
     *
     * @param user 인증된 사용자 정보
     * @param request 체크아웃 요청
     * @return 체크아웃 결과 (지출 정보 포함)
     */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CartCheckoutResponse> checkoutCart(
            @AuthUser AuthenticatedUser user,
            @RequestBody @Valid CartCheckoutRequest request) {
        CartCheckoutResponse response = cartService.checkoutCart(user.memberId(), request);
        return ApiResponse.success(response);
    }
}
