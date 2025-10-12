package com.stdev.smartmealtable.storage.db.favorite;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 우선순위 변경
     * 
     * @param newPriority 새로운 우선순위
     */
    public void changePriority(Long newPriority) {
        this.priority = newPriority;
    }
}
