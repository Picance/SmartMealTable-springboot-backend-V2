package com.stdev.smartmealtable.storage.db.store;

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
@Table(name = "store_category")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreCategoryJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_category_id")
    private Long storeCategoryId;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
