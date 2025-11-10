package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.storage.db.category.CategoryJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 가게(Store)와 카테고리(Category) N:N 관계를 나타내는 JPA 엔티티
 * 중간 테이블(Junction Table) 역할
 */
@Entity
@Table(
    name = "store_category",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_store_category_store_id_category_id",
            columnNames = {"store_id", "category_id"}
        )
    },
    indexes = {
        @Index(name = "idx_store_category_store_id", columnList = "store_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreCategoryJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_category_id")
    private Long storeCategoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "store_id", 
        nullable = false, 
        insertable = false, 
        updatable = false,
        foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)  // 물리 FK 제약조건 비활성화
    )
    private StoreJpaEntity store;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "category_id", 
        nullable = false, 
        insertable = false, 
        updatable = false,
        foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)  // 물리 FK 제약조건 비활성화
    )
    private CategoryJpaEntity category;
    
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    /**
     * 카테고리 표시 순서 (UI에서 카테고리를 표시할 때 사용)
     * 값이 작을수록 먼저 표시됩니다.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
