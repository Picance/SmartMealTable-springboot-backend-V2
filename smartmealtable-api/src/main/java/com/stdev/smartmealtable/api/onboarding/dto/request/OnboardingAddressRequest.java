package com.stdev.smartmealtable.api.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 온보딩 - 주소 등록 요청 DTO
 */
public record OnboardingAddressRequest(
        
        @NotBlank(message = "주소 별칭은 필수입니다.")
        @Size(min = 1, max = 50, message = "주소 별칭은 1~50자 이내여야 합니다.")
        String alias,
        
        @Size(max = 255, message = "지번 주소는 255자 이내여야 합니다.")
        String lotNumberAddress,
        
        @NotBlank(message = "도로명 주소는 필수입니다.")
        @Size(min = 1, max = 255, message = "도로명 주소는 1~255자 이내여야 합니다.")
        String streetNameAddress,
        
        @Size(max = 255, message = "상세 주소는 255자 이내여야 합니다.")
        String detailedAddress,
        
        @NotNull(message = "위도는 필수입니다.")
        Double latitude,
        
        @NotNull(message = "경도는 필수입니다.")
        Double longitude,
        
        @Size(max = 20, message = "주소 유형은 20자 이내여야 합니다.")
        String addressType,
        
        Boolean isPrimary
) {
}
