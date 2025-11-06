package com.stdev.smartmealtable.domain.food;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 음식 도메인 엔티티
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Food {

    private Long foodId;
    private String foodName;
    private Long storeId;
    private Long categoryId;
    private String description;
    private String imageUrl;
    private Integer averagePrice; // 추천 시스템용
    private Integer price; // 크롤러용 개별 가격
    private Boolean isMain; // 대표 메뉴 여부
    private Integer displayOrder; // 표시 순서
    private LocalDateTime registeredDt; // 등록 시각 (비즈니스 필드)
    private LocalDateTime deletedAt; // 삭제 시각 (소프트 삭제)

    /**
     * JPA Entity에서 Domain Entity로 변환 시 사용 (reconstitute 패턴)
     */
    public static Food reconstitute(
            Long foodId,
            String foodName,
            Long storeId,
            Long categoryId,
            String description,
            String imageUrl,
            Integer averagePrice
    ) {
        return Food.builder()
                .foodId(foodId)
                .foodName(foodName)
                .storeId(storeId)
                .categoryId(categoryId)
                .description(description)
                .imageUrl(imageUrl)
                .averagePrice(averagePrice)
                .build();
    }

    /**
     * JPA Entity에서 Domain Entity로 변환 시 사용 (reconstitute 패턴) - v2.0
     * 
     * <p>isMain, displayOrder 필드가 포함됩니다.</p>
     */
    public static Food reconstituteWithMainAndOrder(
            Long foodId,
            String foodName,
            Long storeId,
            Long categoryId,
            String description,
            String imageUrl,
            Integer averagePrice,
            Boolean isMain,
            Integer displayOrder
    ) {
        return Food.builder()
                .foodId(foodId)
                .foodName(foodName)
                .storeId(storeId)
                .categoryId(categoryId)
                .description(description)
                .imageUrl(imageUrl)
                .averagePrice(averagePrice)
                .isMain(isMain != null ? isMain : false)
                .displayOrder(displayOrder)
                .build();
    }

    /**
     * 새로운 음식 생성 (테스트 및 초기화 시 사용) - v2.0
     * 
     * <p>isMain, displayOrder 필드가 추가되었습니다.</p>
     */
    public static Food create(
            String foodName,
            Long storeId,
            Long categoryId,
            String description,
            String imageUrl,
            Integer averagePrice,
            Boolean isMain,
            Integer displayOrder
    ) {
        return Food.builder()
                .foodName(foodName)
                .storeId(storeId)
                .categoryId(categoryId)
                .description(description)
                .imageUrl(imageUrl)
                .averagePrice(averagePrice)
                .isMain(isMain != null ? isMain : false)
                .displayOrder(displayOrder)
                .build();
    }

    /**
     * 음식이 유효한지 검증
     */
    public boolean isValid() {
        return foodName != null && !foodName.trim().isEmpty() 
            && (price != null || averagePrice != null)
            && (price == null || price >= 0)
            && (averagePrice == null || averagePrice >= 0)
            && storeId != null
            && categoryId != null;
    }

    /**
     * 대표 메뉴인지 확인
     */
    public boolean isMainFood() {
        return isMain != null && isMain;
    }

    /**
     * 삭제된 음식인지 확인
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
