package com.stdev.smartmealtable.storage.db.store;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 가게 임시 휴무 JPA 엔티티
 */
@Entity
@Table(name = "store_temporary_closure")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreTemporaryClosureJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_temporary_closure_id")
    private Long storeTemporaryClosureId;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "closure_date", nullable = false)
    private LocalDate closureDate;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Column(name = "reason", length = 200)
    private String reason;
    
    // created_at, updated_at, registered_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리
    @Column(name = "registered_at", insertable = false, updatable = false)
    private LocalDateTime registeredAt;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
