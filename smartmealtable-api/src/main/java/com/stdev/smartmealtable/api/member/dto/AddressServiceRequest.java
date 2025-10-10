package com.stdev.smartmealtable.api.member.dto;

import com.stdev.smartmealtable.domain.common.vo.Address;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 등록/수정 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressServiceRequest {
    
    private String addressAlias;
    private String lotNumberAddress;
    private String streetNameAddress;
    private String detailedAddress;
    private Double latitude;
    private Double longitude;
    private String addressType;
    private Boolean isPrimary;
    
    public AddressServiceRequest(
            String addressAlias,
            String lotNumberAddress,
            String streetNameAddress,
            String detailedAddress,
            Double latitude,
            Double longitude,
            String addressType,
            Boolean isPrimary
    ) {
        this.addressAlias = addressAlias;
        this.lotNumberAddress = lotNumberAddress;
        this.streetNameAddress = streetNameAddress;
        this.detailedAddress = detailedAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressType = addressType;
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }
    
    /**
     * Domain VO로 변환
     */
    public Address toAddress() {
        return Address.of(
                this.addressAlias,
                this.lotNumberAddress,
                this.streetNameAddress,
                this.detailedAddress,
                this.latitude,
                this.longitude,
                this.addressType
        );
    }
}
