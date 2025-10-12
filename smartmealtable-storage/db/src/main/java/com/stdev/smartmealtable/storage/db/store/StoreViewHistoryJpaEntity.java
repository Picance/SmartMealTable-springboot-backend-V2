package com.stdev.smartmealtable.storage.db.store;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 가게 조회 이력 JPA 엔티티
 */
@Entity
@Table(name = "store_view_history")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreViewHistoryJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_view_history_id")
    private Long storeViewHistoryId;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;
    
    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
