package com.stdev.smartmealtable.domain.common.vo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 값 타입 (Value Object)
 * 불변성을 보장하는 주소 정보
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {
    
    private String alias;                    // 주소 별칭 (예: 우리집, 회사)
    private String lotNumberAddress;         // 지번 주소
    private String streetNameAddress;        // 도로명 주소
    private String detailedAddress;          // 상세 주소 (동/호수)
    private Double latitude;                 // 위도
    private Double longitude;                // 경도
    private String addressType;              // 주소 유형 (예: HOME, OFFICE)
    
    /**
     * 주소 생성 (정적 팩토리 메서드)
     */
    public static Address of(
            String alias,
            String lotNumberAddress,
            String streetNameAddress,
            String detailedAddress,
            Double latitude,
            Double longitude,
            String addressType
    ) {
        Address address = new Address();
        address.alias = alias;
        address.lotNumberAddress = lotNumberAddress;
        address.streetNameAddress = streetNameAddress;
        address.detailedAddress = detailedAddress;
        address.latitude = latitude;
        address.longitude = longitude;
        address.addressType = addressType;
        return address;
    }
    
    /**
     * 전체 주소 문자열 반환 (도로명 우선)
     */
    public String getFullAddress() {
        String baseAddress = streetNameAddress != null ? streetNameAddress : lotNumberAddress;
        if (detailedAddress != null && !detailedAddress.isBlank()) {
            return baseAddress + " " + detailedAddress;
        }
        return baseAddress;
    }
    
    /**
     * 주소 유효성 검증
     */
    public boolean isValid() {
        // 도로명 주소 또는 지번 주소 중 하나는 필수
        return (streetNameAddress != null && !streetNameAddress.isBlank()) 
                || (lotNumberAddress != null && !lotNumberAddress.isBlank());
    }
}
