package com.stdev.smartmealtable.storage.db.member.entity;

import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.storage.db.common.vo.AddressEmbeddable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주소 이력 JPA 엔티티
 */
@Entity
@Table(name = "address_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressHistoryJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_history_id")
    private Long addressHistoryId;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Embedded
    private AddressEmbeddable address;
    
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;
    
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;
    
    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    // JPA에서는 읽기 전용으로만 매핑
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Domain → JPA Entity 변환
     */
    public static AddressHistoryJpaEntity from(AddressHistory domain) {
        AddressHistoryJpaEntity entity = new AddressHistoryJpaEntity();
        entity.addressHistoryId = domain.getAddressHistoryId();
        entity.memberId = domain.getMemberId();
        entity.address = AddressEmbeddable.from(domain.getAddress());
        entity.isPrimary = domain.getIsPrimary();
        entity.registeredAt = domain.getRegisteredAt();
        return entity;
    }
    
    /**
     * JPA Entity → Domain 변환
     */
    public AddressHistory toDomain() {
        return AddressHistory.reconstitute(
                this.addressHistoryId,
                this.memberId,
                this.address.toDomain(),
                this.isPrimary,
                this.registeredAt
        );
    }
    
    /**
     * 기본 주소로 설정
     */
    public void markAsPrimary() {
        this.isPrimary = true;
    }
    
    /**
     * 기본 주소 해제
     */
    public void unmarkAsPrimary() {
        this.isPrimary = false;
    }
    
    /**
     * 주소 정보 업데이트
     */
    public void updateAddress(AddressEmbeddable newAddress) {
        this.address = newAddress;
    }
}

