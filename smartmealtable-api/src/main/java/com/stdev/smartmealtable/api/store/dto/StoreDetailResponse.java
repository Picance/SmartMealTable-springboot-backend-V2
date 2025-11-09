package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreImage;
import com.stdev.smartmealtable.domain.store.StoreOpeningHour;
import com.stdev.smartmealtable.domain.store.StoreTemporaryClosure;
import com.stdev.smartmealtable.domain.store.StoreType;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 가게 상세 조회 응답 DTO
 */
public record StoreDetailResponse(
        Long storeId,
        String name,
        Long categoryId,
        String categoryName,
        String address,
        String lotNumberAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        String phoneNumber,
        String description,
        Integer averagePrice,
        Integer reviewCount,
        Integer viewCount,
        Integer favoriteCount,
        StoreType storeType,
        String imageUrl,
        List<StoreImageDto> images,
        List<OpeningHourInfo> openingHours,
        List<TemporaryClosureInfo> temporaryClosures,
        List<MenuInfo> menus,
        Boolean isFavorite,
        LocalDateTime registeredAt
) {
    public static StoreDetailResponse from(
            Store store,
            List<StoreImage> images,
            List<StoreOpeningHour> openingHours,
            List<StoreTemporaryClosure> temporaryClosures,
            List<Food> foods
    ) {
        Long primaryCategoryId = store.getCategoryIds().isEmpty() ? null : store.getCategoryIds().get(0);
        return new StoreDetailResponse(
                store.getStoreId(),
                store.getName(),
                primaryCategoryId,
                null, // TODO: Category 조인 필요
                store.getAddress(),
                store.getLotNumberAddress(),
                store.getLatitude(),
                store.getLongitude(),
                store.getPhoneNumber(),
                store.getDescription(),
                store.getAveragePrice(),
                store.getReviewCount(),
                store.getViewCount(),
                store.getFavoriteCount(),
                store.getStoreType(),
                store.getImageUrl(),
                images.stream().map(StoreImageDto::from).toList(),
                openingHours.stream().map(OpeningHourInfo::from).toList(),
                temporaryClosures.stream().map(TemporaryClosureInfo::from).toList(),
                foods.stream().map(MenuInfo::from).toList(),
                false, // TODO: 즐겨찾기 여부 조회 필요
                store.getRegisteredAt()
        );
    }
    
    /**
     * 영업시간 정보
     */
    public record OpeningHourInfo(
            DayOfWeek dayOfWeek,
            String openTime,
            String closeTime,
            String breakStartTime,
            String breakEndTime,
            Boolean isHoliday
    ) {
        public static OpeningHourInfo from(StoreOpeningHour openingHour) {
            return new OpeningHourInfo(
                    openingHour.dayOfWeek(),
                    openingHour.openTime(),
                    openingHour.closeTime(),
                    openingHour.breakStartTime(),
                    openingHour.breakEndTime(),
                    openingHour.isHoliday()
            );
        }
    }
    
    /**
     * 임시 휴무 정보
     */
    public record TemporaryClosureInfo(
            LocalDate closureDate,
            LocalTime startTime,
            LocalTime endTime,
            String reason
    ) {
        public static TemporaryClosureInfo from(StoreTemporaryClosure closure) {
            return new TemporaryClosureInfo(
                    closure.closureDate(),
                    closure.startTime(),
                    closure.endTime(),
                    closure.reason()
            );
        }
    }

    /**
     * 메뉴(음식) 정보
     */
    public record MenuInfo(
            Long foodId,
            String foodName,
            Integer price,
            String description,
            String imageUrl,
            Boolean isMain,
            Integer displayOrder,
            Boolean isAvailable,
            LocalDateTime registeredDt
    ) {
        public static MenuInfo from(Food food) {
            return new MenuInfo(
                    food.getFoodId(),
                    food.getFoodName(),
                    food.getAveragePrice(),
                    food.getDescription(),
                    food.getImageUrl(),
                    food.getIsMain(),
                    food.getDisplayOrder(),
                    food.getDeletedAt() == null,
                    food.getRegisteredDt()
            );
        }
    }
}
