package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.StoreImage;

/**
 * StoreImage 도메인 ↔ JPA Entity 변환 Mapper
 */
public class StoreImageEntityMapper {
    
    /**
     * Domain → JPA Entity
     */
    public static StoreImageJpaEntity toJpaEntity(StoreImage domain) {
        if (domain == null) {
            return null;
        }
        
        return StoreImageJpaEntity.builder()
                .storeImageId(domain.getStoreImageId())
                .storeId(domain.getStoreId())
                .imageUrl(domain.getImageUrl())
                .isMain(domain.isMain())
                .displayOrder(domain.getDisplayOrder())
                .build();
    }
    
    /**
     * JPA Entity → Domain
     */
    public static StoreImage toDomain(StoreImageJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return StoreImage.builder()
                .storeImageId(entity.getStoreImageId())
                .storeId(entity.getStoreId())
                .imageUrl(entity.getImageUrl())
                .isMain(entity.getIsMain())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }
}
