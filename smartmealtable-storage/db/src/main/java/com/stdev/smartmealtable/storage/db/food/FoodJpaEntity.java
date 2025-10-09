package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.Food;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Column(name = "food_name", nullable = false, length = 100)
    private String foodName;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "average_price")
    private Integer averagePrice;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    // registered_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "registered_at", insertable = false, updatable = false)
    private LocalDateTime registeredAt;

    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA Entity → Domain 변환
     */
    public Food toDomain() {
        return Food.reconstitute(
                this.foodId,
                this.foodName,
                this.categoryId,
                this.description,
                this.imageUrl,
                this.averagePrice
        );
    }

    /**
     * Domain → JPA Entity 변환 (for save)
     */
    public static FoodJpaEntity fromDomain(Food food) {
        FoodJpaEntity entity = new FoodJpaEntity();
        entity.foodId = food.getFoodId();
        entity.foodName = food.getFoodName();
        entity.categoryId = food.getCategoryId();
        entity.description = food.getDescription();
        entity.imageUrl = food.getImageUrl();
        entity.averagePrice = food.getAveragePrice();
        entity.isAvailable = true; // 기본값
        return entity;
    }
}
