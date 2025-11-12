package com.stdev.smartmealtable.domain.cart;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 장바구니 도메인 엔티티
 * 회원이 선택한 음식들을 임시로 담아두는 공간
 * 한 장바구니는 하나의 가게에 대한 음식만 담을 수 있음
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Cart {
    
    /**
     * 장바구니 고유 식별자
     */
    private Long cartId;
    
    /**
     * 회원 ID (외부 참조)
     */
    private Long memberId;
    
    /**
     * 가게 ID (외부 참조)
     * 장바구니에 담긴 음식을 파는 음식점의 식별자
     */
    private Long storeId;
    
    /**
     * 장바구니 항목 리스트
     * Aggregate Root로서 CartItem들을 관리
     */
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    
    /**
     * 정적 팩토리 메서드 - 새 장바구니 생성
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @return 생성된 Cart 엔티티
     */
    public static Cart create(Long memberId, Long storeId) {
        return Cart.builder()
                .memberId(memberId)
                .storeId(storeId)
                .items(new ArrayList<>())
                .build();
    }
    
    /**
     * 장바구니에 아이템 추가
     * 이미 존재하는 음식이면 수량 증가, 없으면 새로 추가
     * 
     * @param foodId 음식 ID
     * @param quantity 수량
     */
    public void addItem(Long foodId, int quantity) {
        validateQuantity(quantity);
        
        // 이미 존재하는 음식인지 확인
        CartItem existingItem = findItemByFoodId(foodId);
        
        if (existingItem != null) {
            // 기존 아이템의 수량 증가
            existingItem.increaseQuantity(quantity);
        } else {
            // 새 아이템 추가
            CartItem newItem = CartItem.create(this.cartId, foodId, quantity);
            items.add(newItem);
        }
    }
    
    /**
     * 장바구니 아이템 수량 변경
     * 
     * @param cartItemId 장바구니 아이템 ID
     * @param newQuantity 새로운 수량
     * @throws IllegalArgumentException 아이템을 찾을 수 없거나 수량이 유효하지 않은 경우
     */
    public void updateItemQuantity(Long cartItemId, int newQuantity) {
        validateQuantity(newQuantity);
        
        CartItem item = findItemById(cartItemId);
        if (item == null) {
            throw new IllegalArgumentException("장바구니에 해당 아이템이 존재하지 않습니다.");
        }
        
        item.updateQuantity(newQuantity);
    }
    
    /**
     * 장바구니에서 아이템 제거
     * 
     * @param cartItemId 제거할 아이템 ID
     * @throws IllegalArgumentException 아이템을 찾을 수 없는 경우
     */
    public void removeItem(Long cartItemId) {
        CartItem item = findItemById(cartItemId);
        if (item == null) {
            throw new IllegalArgumentException("장바구니에 해당 아이템이 존재하지 않습니다.");
        }
        
        items.remove(item);
    }
    
    /**
     * 장바구니 전체 비우기
     */
    public void clear() {
        items.clear();
    }

    /**
     * 다른 가게의 상품이 담겨있는지 확인
     *
     * @param targetStoreId 확인할 가게 ID
     * @return 다른 가게 상품이 있으면 true
     */
    public boolean hasItemsFromDifferentStore(Long targetStoreId) {
        return !this.storeId.equals(targetStoreId) && !isEmpty();
    }

    /**
     * 장바구니가 비어있는지 확인
     *
     * @return 비어있으면 true
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    /**
     * 장바구니 총 아이템 개수 (수량의 합)
     * 
     * @return 총 수량
     */
    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    /**
     * Food ID로 장바구니 아이템 찾기
     */
    private CartItem findItemByFoodId(Long foodId) {
        return items.stream()
                .filter(item -> item.getFoodId().equals(foodId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * CartItem ID로 장바구니 아이템 찾기
     */
    private CartItem findItemById(Long cartItemId) {
        return items.stream()
                .filter(item -> item.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 수량 유효성 검증
     */
    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
    }
}
