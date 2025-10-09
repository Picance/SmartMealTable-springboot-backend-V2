package com.stdev.smartmealtable.domain.member.entity;

import com.stdev.smartmealtable.domain.common.vo.Address;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원의 주소 이력을 관리하는 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressHistory {
    
    private Long addressHistoryId;
    private Long memberId;
    private Address address;              // 주소 값 타입
    private Boolean isPrimary;
    private LocalDateTime registeredAt;   // 비즈니스 필드
    // created_at, updated_at은 감사 로그로 DB 전용 (도메인에 노출 안 함)
    
    /**
     * 새로운 주소 이력 생성 (정적 팩토리 메서드)
     */
    public static AddressHistory create(
            Long memberId,
            Address address,
            Boolean isPrimary
    ) {
        AddressHistory addressHistory = new AddressHistory();
        addressHistory.memberId = memberId;
        addressHistory.address = address;
        addressHistory.isPrimary = isPrimary != null ? isPrimary : false;
        addressHistory.registeredAt = LocalDateTime.now();
        return addressHistory;
    }
    
    /**
     * 기존 주소 이력 재구성 (ID 포함) - JPA 조회 시 사용
     */
    public static AddressHistory reconstitute(
            Long addressHistoryId,
            Long memberId,
            Address address,
            Boolean isPrimary,
            LocalDateTime registeredAt
    ) {
        AddressHistory addressHistory = new AddressHistory();
        addressHistory.addressHistoryId = addressHistoryId;
        addressHistory.memberId = memberId;
        addressHistory.address = address;
        addressHistory.isPrimary = isPrimary;
        addressHistory.registeredAt = registeredAt;
        return addressHistory;
    }
    
    /**
     * 주 주소로 설정
     */
    public void markAsPrimary() {
        this.isPrimary = true;
    }
    
    /**
     * 주 주소 해제
     */
    public void unmarkAsPrimary() {
        this.isPrimary = false;
    }
    
    /**
     * 주소 정보 수정
     */
    public void updateAddress(Address newAddress) {
        this.address = newAddress;
    }
    
    /**
     * 주소 값 타입 반환
     */
    public Address getAddress() {
        return this.address;
    }
}
