package com.stdev.smartmealtable.storage.db.favorite;

import com.stdev.smartmealtable.domain.favorite.Favorite;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 JPA Entity
 */
@Entity
@Table(name = "favorite", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_store_member", columnNames = {"store_id", "member_id"})
       },
       indexes = {
           @Index(name = "idx_priority", columnList = "priority")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "priority", nullable = false)
    private Long priority;
    
    @Column(name = "favorited_at", nullable = false)
    private LocalDateTime favoritedAt;
    
    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 우선순위 변경
     * 
     * @param newPriority 새로운 우선순위
     */
    public void changePriority(Long newPriority) {
        this.priority = newPriority;
    }
    
    /**
     * Domain Entity → JPA Entity 변환 (신규 생성)
     */
    public static FavoriteEntity fromDomain(Favorite favorite) {
        FavoriteEntity entity = new FavoriteEntity();
        entity.memberId = favorite.getMemberId();
        entity.storeId = favorite.getStoreId();
        entity.priority = favorite.getPriority();
        entity.favoritedAt = favorite.getFavoritedAt();
        return entity;
    }
    
    /**
     * Domain Entity → JPA Entity 변환 (ID 보존)
     */
    public static FavoriteEntity fromDomainWithId(Favorite favorite) {
        FavoriteEntity entity = new FavoriteEntity();
        entity.favoriteId = favorite.getFavoriteId();
        entity.memberId = favorite.getMemberId();
        entity.storeId = favorite.getStoreId();
        entity.priority = favorite.getPriority();
        entity.favoritedAt = favorite.getFavoritedAt();
        return entity;
    }
    
    /**
     * JPA Entity → Domain Entity 변환
     */
    public Favorite toDomain() {
        return Favorite.builder()
                .favoriteId(this.favoriteId)
                .memberId(this.memberId)
                .storeId(this.storeId)
                .priority(this.priority)
                .favoritedAt(this.favoritedAt)
                .build();
    }
}
