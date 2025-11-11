package com.stdev.smartmealtable.api.member.dto.response;

import com.stdev.smartmealtable.api.member.dto.AddressServiceResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주소 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressResponse {
    
    private Long addressHistoryId;
    private String addressAlias;
    private String addressType;
    private String lotNumberAddress;
    private String streetNameAddress;
    private String detailedAddress;
    private Double latitude;
    private Double longitude;
    private Boolean isPrimary;
    private LocalDateTime registeredAt;
    
    public AddressResponse(
            Long addressHistoryId,
            String addressAlias,
            String addressType,
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
     * Service Response DTO에서 변환
     */
    public static AddressResponse from(AddressServiceResponse serviceResponse) {
        String addressTypeString = serviceResponse.getAddressType() != null
                ? serviceResponse.getAddressType().name()
                : null;

        return new AddressResponse(
                serviceResponse.getAddressHistoryId(),
                serviceResponse.getAddressAlias(),
                addressTypeString,
                serviceResponse.getLotNumberAddress(),
                serviceResponse.getStreetNameAddress(),
                serviceResponse.getDetailedAddress(),
                serviceResponse.getLatitude(),
                serviceResponse.getLongitude(),
                serviceResponse.getIsPrimary(),
                serviceResponse.getRegisteredAt()
        );
    }
}
