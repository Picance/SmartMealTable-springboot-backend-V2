package com.stdev.smartmealtable.storage.db.expenditure;

import com.stdev.smartmealtable.domain.expenditure.ExpenditureItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 지출 항목 JPA 엔티티
 */
@Entity
@Table(name = "expenditure_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenditureItemJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expenditure_item_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expenditure_id", nullable = false)
    private ExpenditureJpaEntity expenditure;
    
    @Column(name = "food_name", nullable = false, length = 200)
    private String foodName;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "price", nullable = false)
    private Integer price;
    
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
    public static ExpenditureItemJpaEntity from(ExpenditureItem domain, ExpenditureJpaEntity expenditure) {
        ExpenditureItemJpaEntity entity = new ExpenditureItemJpaEntity();
        entity.expenditure = expenditure;
        entity.foodName = domain.getFoodName();
        entity.quantity = domain.getQuantity();
        entity.price = domain.getPrice();
        return entity;
    }
    
    /**
     * JPA Entity → Domain 변환
     */
    public ExpenditureItem toDomain() {
        return ExpenditureItem.create(
                this.foodName,
                this.quantity,
                this.price
        );
    }
    
    /**
     * 양방향 관계 설정
     */
    protected void setExpenditure(ExpenditureJpaEntity expenditure) {
        this.expenditure = expenditure;
    }
}
