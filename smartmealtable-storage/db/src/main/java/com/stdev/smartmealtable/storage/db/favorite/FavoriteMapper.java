package com.stdev.smartmealtable.storage.db.favorite;

import com.stdev.smartmealtable.domain.favorite.Favorite;

/**
 * FavoriteEntity <-> Favorite 도메인 엔티티 매퍼
 */
public class FavoriteMapper {
    
    /**
     * 도메인 엔티티를 JPA 엔티티로 변환
     * 
     * @param favorite 도메인 엔티티
     * @return JPA 엔티티
     */
    public static FavoriteEntity toEntity(Favorite favorite) {
        if (favorite.getFavoriteId() == null) {
            return FavoriteEntity.fromDomain(favorite);
        } else {
            return FavoriteEntity.fromDomainWithId(favorite);
        }
    }
    
    /**
     * JPA 엔티티를 도메인 엔티티로 변환
     * 
     * @param entity JPA 엔티티
     * @return 도메인 엔티티
     */
    public static Favorite toDomain(FavoriteEntity entity) {
        return entity.toDomain();
    }
}
