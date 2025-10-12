package com.stdev.smartmealtable.storage.db.store;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * 가게 영업시간 JPA 엔티티
 */
@Entity
@Table(name = "store_opening_hour")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreOpeningHourJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_opening_hour_id")
    private Long storeOpeningHourId;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;
    
    @Column(name = "open_time", length = 8)
    private String openTime;
    
    @Column(name = "close_time", length = 8)
    private String closeTime;
    
    @Column(name = "break_start_time", length = 8)
    private String breakStartTime;
    
    @Column(name = "break_end_time", length = 8)
    private String breakEndTime;
    
    @Column(name = "is_holiday", nullable = false)
    @Builder.Default
    private Boolean isHoliday = false;
    
    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
