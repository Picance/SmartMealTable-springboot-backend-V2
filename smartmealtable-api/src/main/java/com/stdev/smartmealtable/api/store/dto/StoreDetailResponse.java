package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreOpeningHour;
import com.stdev.smartmealtable.domain.store.StoreTemporaryClosure;
import com.stdev.smartmealtable.domain.store.StoreType;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 가게 상세 조회 응답 DTO
 */
public record StoreDetailResponse(
        Long storeId,
        String name,
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
        List<OpeningHourInfo> openingHours,
        List<TemporaryClosureInfo> temporaryClosures,
        Boolean isFavorite
) {
    public static StoreDetailResponse from(
            Store store,
            List<StoreOpeningHour> openingHours,
            List<StoreTemporaryClosure> temporaryClosures
    ) {
        return new StoreDetailResponse(
                store.getStoreId(),
                store.getName(),
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
                openingHours.stream().map(OpeningHourInfo::from).toList(),
                temporaryClosures.stream().map(TemporaryClosureInfo::from).toList(),
                false // TODO: 즐겨찾기 여부 조회 필요
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
}
