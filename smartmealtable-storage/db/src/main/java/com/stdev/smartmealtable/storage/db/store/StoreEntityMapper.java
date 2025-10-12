package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreOpeningHour;
import com.stdev.smartmealtable.domain.store.StoreTemporaryClosure;
import com.stdev.smartmealtable.domain.store.StoreViewHistory;

/**
 * Store 엔티티와 JPA 엔티티 간 변환 Mapper
 */
public final class StoreEntityMapper {
    
    private StoreEntityMapper() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Domain Store → JPA Entity
     */
    public static StoreJpaEntity toJpaEntity(Store store) {
        return StoreJpaEntity.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .categoryId(store.getCategoryId())
                .sellerId(store.getSellerId())
                .address(store.getAddress())
                .lotNumberAddress(store.getLotNumberAddress())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .phoneNumber(store.getPhoneNumber())
                .description(store.getDescription())
                .averagePrice(store.getAveragePrice())
                .reviewCount(store.getReviewCount())
                .viewCount(store.getViewCount())
                .favoriteCount(store.getFavoriteCount())
                .storeType(store.getStoreType())
                .imageUrl(store.getImageUrl())
                .registeredAt(store.getRegisteredAt())
                .deletedAt(store.getDeletedAt())
                .build();
    }
    
    /**
     * JPA Entity → Domain Store
     */
    public static Store toDomain(StoreJpaEntity entity) {
        return Store.builder()
                .storeId(entity.getStoreId())
                .name(entity.getName())
                .categoryId(entity.getCategoryId())
                .sellerId(entity.getSellerId())
                .address(entity.getAddress())
                .lotNumberAddress(entity.getLotNumberAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .phoneNumber(entity.getPhoneNumber())
                .description(entity.getDescription())
                .averagePrice(entity.getAveragePrice())
                .reviewCount(entity.getReviewCount())
                .viewCount(entity.getViewCount())
                .favoriteCount(entity.getFavoriteCount())
                .storeType(entity.getStoreType())
                .imageUrl(entity.getImageUrl())
                .registeredAt(entity.getRegisteredAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
    
    /**
     * Domain StoreOpeningHour → JPA Entity
     */
    public static StoreOpeningHourJpaEntity toJpaEntity(StoreOpeningHour openingHour) {
        return StoreOpeningHourJpaEntity.builder()
                .storeOpeningHourId(openingHour.storeOpeningHourId())
                .storeId(openingHour.storeId())
                .dayOfWeek(openingHour.dayOfWeek())
                .openTime(openingHour.openTime())
                .closeTime(openingHour.closeTime())
                .breakStartTime(openingHour.breakStartTime())
                .breakEndTime(openingHour.breakEndTime())
                .isHoliday(openingHour.isHoliday())
                .build();
    }
    
    /**
     * JPA Entity → Domain StoreOpeningHour
     */
    public static StoreOpeningHour toDomain(StoreOpeningHourJpaEntity entity) {
        return new StoreOpeningHour(
                entity.getStoreOpeningHourId(),
                entity.getStoreId(),
                entity.getDayOfWeek(),
                entity.getOpenTime(),
                entity.getCloseTime(),
                entity.getBreakStartTime(),
                entity.getBreakEndTime(),
                entity.getIsHoliday()
        );
    }
    
    /**
     * Domain StoreTemporaryClosure → JPA Entity
     */
    public static StoreTemporaryClosureJpaEntity toJpaEntity(StoreTemporaryClosure closure) {
        return StoreTemporaryClosureJpaEntity.builder()
                .storeTemporaryClosureId(closure.storeTemporaryClosureId())
                .storeId(closure.storeId())
                .closureDate(closure.closureDate())
                .startTime(closure.startTime())
                .endTime(closure.endTime())
                .reason(closure.reason())
                .build();
    }
    
    /**
     * JPA Entity → Domain StoreTemporaryClosure
     */
    public static StoreTemporaryClosure toDomain(StoreTemporaryClosureJpaEntity entity) {
        return new StoreTemporaryClosure(
                entity.getStoreTemporaryClosureId(),
                entity.getStoreId(),
                entity.getClosureDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getReason()
        );
    }
    
    /**
     * Domain StoreViewHistory → JPA Entity
     */
    public static StoreViewHistoryJpaEntity toJpaEntity(StoreViewHistory viewHistory) {
        return StoreViewHistoryJpaEntity.builder()
                .storeViewHistoryId(viewHistory.storeViewHistoryId())
                .storeId(viewHistory.storeId())
                .memberId(viewHistory.memberId())
                .viewedAt(viewHistory.viewedAt())
                .build();
    }
    
    /**
     * JPA Entity → Domain StoreViewHistory
     */
    public static StoreViewHistory toDomain(StoreViewHistoryJpaEntity entity) {
        return new StoreViewHistory(
                entity.getStoreViewHistoryId(),
                entity.getStoreId(),
                entity.getMemberId(),
                entity.getViewedAt()
        );
    }
}
