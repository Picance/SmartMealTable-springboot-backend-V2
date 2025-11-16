package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.Food;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 음식 JPA Entity
 */
@Entity
@Table(name = "food")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "food_name", nullable = false, length = 100)
    private String foodName;

    @Column(
            name = "food_name_normalized",
            insertable = false,
            updatable = false,
            columnDefinition = "varchar(100) GENERATED ALWAYS AS (lower(regexp_replace(food_name, '[^0-9a-z가-힣]', ''))) STORED"
    )
    private String foodNameNormalized;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_main", nullable = false)
    private Boolean isMain;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "registered_dt", insertable = false, updatable = false)
    private LocalDateTime registeredDt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Builder
    private FoodJpaEntity(Long foodId, Long storeId, String foodName, Integer price, Long categoryId,
                          String description, String imageUrl, Boolean isMain, Integer displayOrder,
                          LocalDateTime registeredDt, LocalDateTime deletedAt) {
        this.foodId = foodId;
        this.storeId = storeId;
        this.foodName = foodName;
        this.price = price;
        this.categoryId = categoryId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isMain = isMain != null ? isMain : false;
        this.displayOrder = displayOrder;
        this.registeredDt = registeredDt;
        this.deletedAt = deletedAt;
    }

    /**
     * JPA Entity → Domain 변환
     */
    public Food toDomain() {
        return Food.builder()
                .foodId(this.foodId)
                .foodName(this.foodName)
                .storeId(this.storeId)
                .categoryId(this.categoryId)
                .description(this.description)
                .imageUrl(this.imageUrl)
                .averagePrice(this.price) // price를 averagePrice로 매핑
                .price(this.price)
                .isMain(this.isMain)
                .displayOrder(this.displayOrder)
                .registeredDt(this.registeredDt)
                .deletedAt(this.deletedAt)
                .build();
    }

    /**
     * Domain → JPA Entity 변환 (for save)
     */
    public static FoodJpaEntity fromDomain(Food food) {
        return FoodJpaEntity.builder()
                .foodId(food.getFoodId())
                .storeId(food.getStoreId())
                .categoryId(food.getCategoryId())
                .foodName(food.getFoodName())
                .price(food.getPrice() != null ? food.getPrice() : food.getAveragePrice())
                .description(food.getDescription())
                .imageUrl(food.getImageUrl())
                .isMain(food.getIsMain() != null ? food.getIsMain() : false)
                .displayOrder(food.getDisplayOrder())
                .registeredDt(food.getRegisteredDt())
                .deletedAt(food.getDeletedAt())
                .build();
    }
}
