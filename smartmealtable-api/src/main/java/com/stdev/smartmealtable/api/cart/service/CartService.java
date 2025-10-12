package com.stdev.smartmealtable.api.cart.service;

import com.stdev.smartmealtable.api.cart.dto.*;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.cart.Cart;
import com.stdev.smartmealtable.domain.cart.CartDomainService;
import com.stdev.smartmealtable.domain.cart.CartItem;
import com.stdev.smartmealtable.domain.cart.CartRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 장바구니 Application Service
 * 유즈케이스에 집중하며 도메인 서비스를 조합하여 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartDomainService cartDomainService;
    private final StoreRepository storeRepository;
    private final FoodRepository foodRepository;
    
    /**
     * 장바구니에 아이템 추가
     * 
     * @param memberId 회원 ID
     * @param request 장바구니 아이템 추가 요청
     * @return 추가된 아이템 정보
     */
    @Transactional
    public AddCartItemResponse addCartItem(Long memberId, AddCartItemRequest request) {
        Long storeId = request.getStoreId();
        Long foodId = request.getFoodId();
        int quantity = request.getQuantity();
        
        // 1. 입력 유효성 검증
        if (quantity <= 0) {
            throw new BusinessException(ErrorType.INVALID_INPUT_VALUE);
        }
        
        // 2. 가게 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorType.STORE_NOT_FOUND));
        
        // 3. 음식 존재 여부 확인
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new BusinessException(ErrorType.FOOD_NOT_FOUND));
        
        // 4. 장바구니 조회 또는 생성
        Cart cart = cartDomainService.getOrCreateCart(memberId, storeId);
        
        // 5. 아이템 추가 (도메인 로직에서 중복 처리)
        cart.addItem(foodId, quantity);
        
        // 6. 저장
        Cart savedCart = cartRepository.save(cart);
        
        // 7. 추가된 아이템 찾기
        CartItem addedItem = savedCart.getItems().stream()
                .filter(item -> item.getFoodId().equals(foodId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorType.INTERNAL_SERVER_ERROR));
        
        return AddCartItemResponse.builder()
                .cartId(savedCart.getCartId())
                .cartItemId(addedItem.getCartItemId())
                .foodId(addedItem.getFoodId())
                .quantity(addedItem.getQuantity())
                .build();
    }
    
    /**
     * 장바구니 조회
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @return 장바구니 정보
     */
    public GetCartResponse getCart(Long memberId, Long storeId) {
        // 1. 장바구니 조회
        Cart cart = cartRepository.findByMemberIdAndStoreId(memberId, storeId)
                .orElseThrow(() -> new BusinessException(ErrorType.CART_NOT_FOUND));
        
        // 2. 가게 정보 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorType.STORE_NOT_FOUND));
        
        // 3. 음식 정보 조회
        List<Long> foodIds = cart.getItems().stream()
                .map(CartItem::getFoodId)
                .collect(Collectors.toList());
        
        Map<Long, Food> foodMap = foodRepository.findByIdIn(foodIds).stream()
                .collect(Collectors.toMap(Food::getFoodId, food -> food));
        
        // 4. 응답 생성
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    Food food = foodMap.get(item.getFoodId());
                    if (food == null) {
                        throw new BusinessException(ErrorType.FOOD_NOT_FOUND);
                    }
                    
                    int subtotal = (food.getAveragePrice() != null ? food.getAveragePrice() : 0) * item.getQuantity();
                    
                    return CartItemResponse.builder()
                            .cartItemId(item.getCartItemId())
                            .foodId(food.getFoodId())
                            .foodName(food.getFoodName())
                            .imageUrl(food.getImageUrl())
                            .averagePrice(food.getAveragePrice())
                            .quantity(item.getQuantity())
                            .subtotal(subtotal)
                            .build();
                })
                .collect(Collectors.toList());
        
        int totalAmount = itemResponses.stream()
                .mapToInt(CartItemResponse::getSubtotal)
                .sum();
        
        return GetCartResponse.builder()
                .cartId(cart.getCartId())
                .storeId(cart.getStoreId())
                .storeName(store.getName())
                .items(itemResponses)
                .totalQuantity(cart.getTotalQuantity())
                .totalAmount(totalAmount)
                .build();
    }
    
    /**
     * 회원의 모든 장바구니 조회
     * 
     * @param memberId 회원 ID
     * @return 장바구니 목록
     */
    public List<GetCartResponse> getAllCarts(Long memberId) {
        // 1. 회원의 모든 장바구니 조회
        List<Cart> carts = cartRepository.findByMemberId(memberId);
        
        if (carts.isEmpty()) {
            return List.of();
        }
        
        // 2. 가게 정보 조회
        List<Long> storeIds = carts.stream()
                .map(Cart::getStoreId)
                .collect(Collectors.toList());
        
        Map<Long, Store> storeMap = storeIds.stream()
                .map(storeRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toMap(Store::getStoreId, store -> store));
        
        // 3. 음식 정보 조회
        List<Long> allFoodIds = carts.stream()
                .flatMap(cart -> cart.getItems().stream())
                .map(CartItem::getFoodId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Food> foodMap = foodRepository.findByIdIn(allFoodIds).stream()
                .collect(Collectors.toMap(Food::getFoodId, food -> food));
        
        // 4. 응답 생성
        return carts.stream()
                .map(cart -> {
                    Store store = storeMap.get(cart.getStoreId());
                    if (store == null) {
                        return null; // 가게가 삭제된 경우 스킵
                    }
                    
                    List<CartItemResponse> itemResponses = cart.getItems().stream()
                            .map(item -> {
                                Food food = foodMap.get(item.getFoodId());
                                if (food == null) {
                                    return null; // 음식이 삭제된 경우 스킵
                                }
                                
                                int subtotal = (food.getAveragePrice() != null ? food.getAveragePrice() : 0) * item.getQuantity();
                                
                                return CartItemResponse.builder()
                                        .cartItemId(item.getCartItemId())
                                        .foodId(food.getFoodId())
                                        .foodName(food.getFoodName())
                                        .imageUrl(food.getImageUrl())
                                        .averagePrice(food.getAveragePrice())
                                        .quantity(item.getQuantity())
                                        .subtotal(subtotal)
                                        .build();
                            })
                            .filter(item -> item != null)
                            .collect(Collectors.toList());
                    
                    int totalAmount = itemResponses.stream()
                            .mapToInt(CartItemResponse::getSubtotal)
                            .sum();
                    
                    return GetCartResponse.builder()
                            .cartId(cart.getCartId())
                            .storeId(cart.getStoreId())
                            .storeName(store.getName())
                            .items(itemResponses)
                            .totalQuantity(cart.getTotalQuantity())
                            .totalAmount(totalAmount)
                            .build();
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 장바구니 아이템 수량 수정
     * 
     * @param memberId 회원 ID
     * @param cartItemId 장바구니 아이템 ID
     * @param request 수량 수정 요청
     */
    @Transactional
    public void updateCartItemQuantity(Long memberId, Long cartItemId, UpdateCartItemQuantityRequest request) {
        int newQuantity = request.getQuantity();
        
        // 1. 입력 유효성 검증
        if (newQuantity <= 0) {
            throw new BusinessException(ErrorType.INVALID_INPUT_VALUE);
        }
        
        // 2. 회원의 장바구니에서 아이템 찾기
        List<Cart> carts = cartRepository.findByMemberId(memberId);
        
        Cart targetCart = null;
        for (Cart cart : carts) {
            boolean found = cart.getItems().stream()
                    .anyMatch(item -> item.getCartItemId().equals(cartItemId));
            if (found) {
                targetCart = cart;
                break;
            }
        }
        
        if (targetCart == null) {
            throw new BusinessException(ErrorType.CART_ITEM_NOT_FOUND);
        }
        
        // 3. 수량 변경
        targetCart.updateItemQuantity(cartItemId, newQuantity);
        
        // 4. 저장
        cartRepository.save(targetCart);
    }
    
    /**
     * 장바구니 아이템 삭제
     * 
     * @param memberId 회원 ID
     * @param cartItemId 장바구니 아이템 ID
     */
    @Transactional
    public void removeCartItem(Long memberId, Long cartItemId) {
        // 1. 회원의 장바구니에서 아이템 찾기
        List<Cart> carts = cartRepository.findByMemberId(memberId);
        
        Cart targetCart = null;
        for (Cart cart : carts) {
            boolean found = cart.getItems().stream()
                    .anyMatch(item -> item.getCartItemId().equals(cartItemId));
            if (found) {
                targetCart = cart;
                break;
            }
        }
        
        if (targetCart == null) {
            throw new BusinessException(ErrorType.CART_ITEM_NOT_FOUND);
        }
        
        // 2. 아이템 제거
        targetCart.removeItem(cartItemId);
        
        // 3. 장바구니가 비었으면 장바구니 자체를 삭제, 아니면 저장
        if (targetCart.isEmpty()) {
            cartRepository.delete(targetCart);
        } else {
            cartRepository.save(targetCart);
        }
    }
    
    /**
     * 장바구니 전체 삭제 (비우기)
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     */
    @Transactional
    public void clearCart(Long memberId, Long storeId) {
        cartDomainService.clearCart(memberId, storeId);
    }
}
