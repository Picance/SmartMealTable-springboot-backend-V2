package com.stdev.smartmealtable.domain.expenditure;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지출 항목 도메인 엔티티
 * - 하나의 지출 내역에 포함된 개별 음식 정보
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenditureItem {
    
    private Long expenditureItemId;
    private Long expenditureId;
    private Long foodId;
    private Integer quantity;
    private Integer price;
    
    /**
     * 지출 항목 생성 - 정적 팩토리 메서드
     */
    public static ExpenditureItem create(
            Long foodId,
            Integer quantity,
            Integer price
    ) {
        ExpenditureItem item = new ExpenditureItem();
        item.foodId = foodId;
        item.quantity = quantity;
        item.price = price;
        
        // 비즈니스 규칙 검증
        item.validateQuantity();
        item.validatePrice();
        
        return item;
    }
    
    /**
     * 지출 항목 재구성 - JPA 엔티티로부터 도메인 복원용
     */
    public static ExpenditureItem reconstruct(
            Long expenditureItemId,
            Long expenditureId,
            Long foodId,
            Integer quantity,
            Integer price
    ) {
        ExpenditureItem item = new ExpenditureItem();
        item.expenditureItemId = expenditureItemId;
        item.expenditureId = expenditureId;
        item.foodId = foodId;
        item.quantity = quantity;
        item.price = price;
        
        return item;
    }
    
    /**
     * 지출 ID 설정 (Aggregate 내부에서만 사용)
     */
    public void setExpenditureId(Long expenditureId) {
        this.expenditureId = expenditureId;
    }
    
    /**
     * 비즈니스 규칙: 수량은 1 이상이어야 함
     */
    private void validateQuantity() {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
    }
    
    /**
     * 비즈니스 규칙: 가격은 0 이상이어야 함
     */
    private void validatePrice() {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
    }
    
    /**
     * 항목의 총 금액 계산
     */
    public int getTotalAmount() {
        return quantity * price;
    }
}
