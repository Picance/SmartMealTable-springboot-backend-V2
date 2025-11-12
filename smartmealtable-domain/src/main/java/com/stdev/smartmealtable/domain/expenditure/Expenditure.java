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
    private Long storeId;           // ◆ 새로 추가 (논리 FK, nullable)
    private String storeName;
    private Integer amount;
    private Long discount;          // ◆ 할인액 (기본값: 0)
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
            Long discount,
            List<ExpenditureItem> items
    ) {
        Expenditure expenditure = new Expenditure();
        expenditure.memberId = memberId;
        expenditure.storeId = null;  // 기존 메서드는 storeId = NULL (호환성)
        expenditure.storeName = storeName;
        expenditure.amount = amount;
        expenditure.discount = discount != null ? discount : 0L;
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
     * 팩토리 메서드: 장바구니 → 지출 등록 (storeId 포함)
     */
    public static Expenditure createFromCart(
            Long memberId,
            Long storeId,           // ◆ store FK 포함
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            Long discount,
            List<ExpenditureItem> items
    ) {
        Expenditure expenditure = new Expenditure();
        expenditure.memberId = memberId;
        expenditure.storeId = storeId;      // ◆ 장바구니에서 넘어온 storeId
        expenditure.storeName = storeName;
        expenditure.amount = amount;
        expenditure.discount = discount != null ? discount : 0L;
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
     * 팩토리 메서드: 수기 입력 → 지출 등록 (storeId = NULL)
     */
    public static Expenditure createFromManualInput(
            Long memberId,
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            Long discount,
            List<ExpenditureItem> items
    ) {
        Expenditure expenditure = new Expenditure();
        expenditure.memberId = memberId;
        expenditure.storeId = null;         // ◆ 수기 입력은 storeId = NULL
        expenditure.storeName = storeName;
        expenditure.amount = amount;
        expenditure.discount = discount != null ? discount : 0L;
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
            Long storeId,           // ◆ 새로 추가
            String storeName,
            Integer amount,
            Long discount,
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
        expenditure.storeId = storeId;      // ◆ 복원
        expenditure.storeName = storeName;
        expenditure.amount = amount;
        expenditure.discount = discount != null ? discount : 0L;
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
            Long discount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            List<ExpenditureItem> items
    ) {
        this.storeName = storeName;
        this.amount = amount;
        this.discount = discount != null ? discount : 0L;
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
     * 비즈니스 규칙: 항목들의 총액 - 할인액 = 지출 금액
     * items[0].price * items[0].quantity + ... - discount = amount
     */
    private void validateItemsTotalAmount() {
        if (items == null || items.isEmpty()) {
            return; // 항목이 없는 경우는 허용 (간단한 지출 기록)
        }

        int itemsTotal = items.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        long discountAmount = discount != null ? discount : 0L;
        long expectedAmount = itemsTotal - discountAmount;

        if (expectedAmount != amount) {
            throw new IllegalArgumentException(
                    String.format("항목 총액(%d) - 할인액(%d) = 지출 금액(%d)과 일치하지 않습니다.", itemsTotal, discountAmount, amount)
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
