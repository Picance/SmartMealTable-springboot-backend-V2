package com.stdev.smartmealtable.storage.db.expenditure;

import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureItem;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 내역 JPA 엔티티
 */
@Entity
@Table(name = "expenditure")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenditureJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expenditure_id")
    private Long id;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Column(name = "store_id")                      // ◆ nullable
    private Long storeId;
    
    @Column(name = "store_name", nullable = false, length = 200)
    private String storeName;
    
    @Column(name = "amount", nullable = false)
    private Integer amount;
    
    @Column(name = "expended_date", nullable = false)
    private LocalDate expendedDate;
    
    @Column(name = "expended_time")
    private LocalTime expendedTime;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", length = 20)
    private MealType mealType;
    
    @Column(name = "memo", length = 500)
    private String memo;
    
    @OneToMany(mappedBy = "expenditure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenditureItemJpaEntity> items = new ArrayList<>();
    
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Domain → JPA Entity 변환
     */
    public static ExpenditureJpaEntity from(Expenditure domain) {
        ExpenditureJpaEntity entity = new ExpenditureJpaEntity();
        entity.id = domain.getExpenditureId();
        entity.memberId = domain.getMemberId();
        entity.storeId = domain.getStoreId();        // ◆ 추가
        entity.storeName = domain.getStoreName();
        entity.amount = domain.getAmount();
        entity.expendedDate = domain.getExpendedDate();
        entity.expendedTime = domain.getExpendedTime();
        entity.categoryId = domain.getCategoryId();
        entity.mealType = domain.getMealType();
        entity.memo = domain.getMemo();
        entity.deleted = domain.getDeleted();
        
        // 항목 변환 및 양방향 관계 설정
        if (domain.getItems() != null) {
            List<ExpenditureItemJpaEntity> itemEntities = domain.getItems().stream()
                    .map(item -> ExpenditureItemJpaEntity.from(item, entity))
                    .collect(Collectors.toList());
            entity.items = itemEntities;
        }
        
        return entity;
    }
    
    /**
     * JPA Entity → Domain 변환
     */
    public Expenditure toDomain() {
        List<ExpenditureItem> domainItems = items.stream()
                .map(ExpenditureItemJpaEntity::toDomain)
                .collect(Collectors.toList());
        
        return Expenditure.reconstruct(
                this.id,
                this.memberId,
                this.storeId,           // ◆ 추가
                this.storeName,
                this.amount,
                this.expendedDate,
                this.expendedTime,
                this.categoryId,
                this.mealType,
                this.memo,
                domainItems,
                this.createdAt,
                this.deleted
        );
    }
    
    /**
     * 양방향 관계 편의 메서드
     */
    public void addItem(ExpenditureItemJpaEntity item) {
        items.add(item);
        item.setExpenditure(this);
    }
}
