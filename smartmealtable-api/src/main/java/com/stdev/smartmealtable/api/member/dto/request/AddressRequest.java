package com.stdev.smartmealtable.api.member.dto.request;

import com.stdev.smartmealtable.api.member.dto.AddressServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 추가/수정 요청 DTO
 * isPrimary 필드는 제거됨 - 첫 주소는 자동으로 기본 주소로 설정됨
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressRequest {
    
    @NotBlank(message = "주소 별칭은 필수입니다.")
    @Size(max = 50, message = "주소 별칭은 최대 50자까지 입력 가능합니다.")
    private String addressAlias;
    
    private String lotNumberAddress;
    
    @NotBlank(message = "도로명 주소는 필수입니다.")
    @Size(max = 255, message = "도로명 주소는 최대 255자까지 입력 가능합니다.")
    private String streetNameAddress;
    
    @Size(max = 255, message = "상세 주소는 최대 255자까지 입력 가능합니다.")
    private String detailedAddress;
    
    private Double latitude;
    private Double longitude;
    
    @Size(max = 20, message = "주소 유형은 최대 20자까지 입력 가능합니다.")
    private String addressType;
    
    public AddressRequest(
            String addressAlias,
            String lotNumberAddress,
            String streetNameAddress,
            String detailedAddress,
            Double latitude,
            Double longitude,
            String addressType
    ) {
        this.addressAlias = addressAlias;
        this.lotNumberAddress = lotNumberAddress;
        this.streetNameAddress = streetNameAddress;
        this.detailedAddress = detailedAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressType = addressType;
    }
    
    /**
     * Service Request DTO로 변환
     */
    public AddressServiceRequest toServiceRequest() {
        return new AddressServiceRequest(
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
