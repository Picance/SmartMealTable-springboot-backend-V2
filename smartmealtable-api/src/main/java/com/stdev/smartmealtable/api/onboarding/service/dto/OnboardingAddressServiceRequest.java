package com.stdev.smartmealtable.api.onboarding.service.dto;

import com.stdev.smartmealtable.domain.common.vo.Address;

/**
 * 온보딩 - 주소 등록 Service Request DTO
 */
public record OnboardingAddressServiceRequest(
        Long memberId,
        Address address,
        Boolean isPrimary
) {
    public static OnboardingAddressServiceRequest of(
            Long memberId,
            String alias,
            String lotNumberAddress,
            String streetNameAddress,
            String detailedAddress,
            Double latitude,
            Double longitude,
            String addressType,
            Boolean isPrimary
    ) {
        Address address = Address.of(
                alias,
                lotNumberAddress,
                streetNameAddress,
                detailedAddress,
                latitude,
                longitude,
                addressType
        );
        
        return new OnboardingAddressServiceRequest(
                memberId,
                address,
                isPrimary
        );
    }
}
