package com.stdev.smartmealtable.domain.expenditure;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 지출 내역 도메인 엔티티
 * - 회원의 음식 관련 지출을 기록하는 Aggregate Root
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expenditure {
    
    private Long expenditureId;
    private Long memberId;
    private String storeName;
    private Integer amount;
    private LocalDate expendedDate;
    private LocalTime expendedTime;
    private Long categoryId;
    private MealType mealType;
    private String memo;
    private List<ExpenditureItem> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private Boolean deleted;
    
    /**
     * 지출 내역 생성 - 정적 팩토리 메서드
     */
    public static Expenditure create(
            Long memberId,
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            List<ExpenditureItem> items
    ) {
        Expenditure expenditure = new Expenditure();
        expenditure.memberId = memberId;
        expenditure.storeName = storeName;
        expenditure.amount = amount;
        expenditure.expendedDate = expendedDate;
        expenditure.expendedTime = expendedTime;
        expenditure.categoryId = categoryId;
        expenditure.mealType = mealType;
        expenditure.memo = memo;
        expenditure.items = new ArrayList<>(items);
        expenditure.deleted = false;
        
        // 비즈니스 규칙 검증
        expenditure.validateAmount();
        expenditure.validateItemsTotalAmount();
        
        return expenditure;
    }
    
    /**
     * 지출 내역 재구성 - JPA 엔티티로부터 도메인 복원용 (검증 스킵)
     */
    public static Expenditure reconstruct(
            Long expenditureId,
            Long memberId,
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            List<ExpenditureItem> items,
            LocalDateTime createdAt,
            Boolean deleted
    ) {
        Expenditure expenditure = new Expenditure();
        expenditure.expenditureId = expenditureId;
        expenditure.memberId = memberId;
        expenditure.storeName = storeName;
        expenditure.amount = amount;
        expenditure.expendedDate = expendedDate;
        expenditure.expendedTime = expendedTime;
        expenditure.categoryId = categoryId;
        expenditure.mealType = mealType;
        expenditure.memo = memo;
        expenditure.items = new ArrayList<>(items);
        expenditure.createdAt = createdAt;
        expenditure.deleted = deleted;
        
        return expenditure;
    }
    
    /**
     * 지출 내역 수정
     */
    public void update(
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            List<ExpenditureItem> items
    ) {
        this.storeName = storeName;
        this.amount = amount;
        this.expendedDate = expendedDate;
        this.expendedTime = expendedTime;
        this.categoryId = categoryId;
        this.mealType = mealType;
        this.memo = memo;
        this.items = new ArrayList<>(items);
        
        // 비즈니스 규칙 검증
        validateAmount();
        validateItemsTotalAmount();
    }
    
    /**
     * 소프트 삭제
     */
    public void delete() {
        this.deleted = true;
    }
    
    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deleted;
    }
    
    /**
     * 소유권 검증 - 해당 회원의 지출인지 확인
     */
    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }
    
    /**
     * 비즈니스 규칙: 금액은 0 이상이어야 함
     */
    private void validateAmount() {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("지출 금액은 0 이상이어야 합니다.");
        }
    }
    
    /**
     * 비즈니스 규칙: 항목들의 총액이 지출 금액과 일치해야 함
     */
    private void validateItemsTotalAmount() {
        if (items == null || items.isEmpty()) {
            return; // 항목이 없는 경우는 허용 (간단한 지출 기록)
        }
        
        int itemsTotal = items.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        if (itemsTotal != amount) {
            throw new IllegalArgumentException(
                    String.format("항목 총액(%d)이 지출 금액(%d)과 일치하지 않습니다.", itemsTotal, amount)
            );
        }
    }
    
    /**
     * 항목 추가
     */
    public void addItem(ExpenditureItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }
}
