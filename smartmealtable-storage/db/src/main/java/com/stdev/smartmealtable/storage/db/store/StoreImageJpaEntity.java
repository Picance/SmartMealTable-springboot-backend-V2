package com.stdev.smartmealtable.storage.db.store;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 가게 이미지 JPA 엔티티
 */
@Entity
@Table(name = "store_image")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreImageJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_image_id")
    private Long storeImageId;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    
    @Column(name = "is_main", nullable = false)
    @Builder.Default
    private Boolean isMain = false;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
