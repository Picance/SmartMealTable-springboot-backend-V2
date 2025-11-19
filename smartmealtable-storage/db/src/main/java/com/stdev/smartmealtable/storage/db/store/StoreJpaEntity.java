package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.StoreType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 음식점(가게) JPA 엔티티
 */
@Entity
@Table(name = "store")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;
    
    @Column(name = "external_id", unique = true, length = 50)
    private String externalId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(
            name = "name_normalized",
            insertable = false,
            updatable = false,
            columnDefinition = "varchar(100) GENERATED ALWAYS AS (lower(regexp_replace(name, '[^0-9a-z가-힣]', ''))) STORED"
    )
    private String nameNormalized;
    
    @Column(name = "seller_id")
    private Long sellerId;
    
    @Column(name = "address", nullable = false, length = 200)
    private String address;
    
    @Column(name = "lot_number_address", length = 200)
    private String lotNumberAddress;
    
    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;
    
    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "average_price")
    private Integer averagePrice;
    
    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;
    
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;
    
    @Column(name = "favorite_count", nullable = false)
    @Builder.Default
    private Integer favoriteCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "store_type", nullable = false, length = 20)
    private StoreType storeType;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
}
