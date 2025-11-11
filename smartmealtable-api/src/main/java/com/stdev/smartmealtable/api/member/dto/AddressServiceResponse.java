package com.stdev.smartmealtable.api.member.dto;

import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주소 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressServiceResponse {

    private Long addressHistoryId;
    private String addressAlias;
    private AddressType addressType;
    private String lotNumberAddress;
    private String streetNameAddress;
    private String detailedAddress;
    private Double latitude;
    private Double longitude;
    private Boolean isPrimary;
    private LocalDateTime registeredAt;
    
    public AddressServiceResponse(
            Long addressHistoryId,
            String addressAlias,
            AddressType addressType,
            String lotNumberAddress,
            String streetNameAddress,
            String detailedAddress,
            Double latitude,
            Double longitude,
            Boolean isPrimary,
            LocalDateTime registeredAt
    ) {
        this.addressHistoryId = addressHistoryId;
        this.addressAlias = addressAlias;
        this.addressType = addressType;
        this.lotNumberAddress = lotNumberAddress;
        this.streetNameAddress = streetNameAddress;
        this.detailedAddress = detailedAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isPrimary = isPrimary;
        this.registeredAt = registeredAt;
    }
    
    /**
     * Domain Entity에서 DTO로 변환
     */
    public static AddressServiceResponse from(AddressHistory addressHistory) {
        return new AddressServiceResponse(
                addressHistory.getAddressHistoryId(),
                addressHistory.getAddress().getAlias(),
                addressHistory.getAddress().getAddressType(),
                addressHistory.getAddress().getLotNumberAddress(),
                addressHistory.getAddress().getStreetNameAddress(),
                addressHistory.getAddress().getDetailedAddress(),
                addressHistory.getAddress().getLatitude(),
                addressHistory.getAddress().getLongitude(),
                addressHistory.getIsPrimary(),
                addressHistory.getRegisteredAt()
        );
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
}
