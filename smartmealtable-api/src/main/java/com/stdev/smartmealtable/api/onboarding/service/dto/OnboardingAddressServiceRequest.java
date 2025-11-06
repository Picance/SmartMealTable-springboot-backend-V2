package com.stdev.smartmealtable.api.onboarding.service.dto;

import com.stdev.smartmealtable.domain.common.vo.Address;

/**
 * 온보딩 - 주소 등록 Service Request DTO
 * isPrimary 필드는 제거됨 - 첫 주소는 자동으로 기본 주소로 설정됨
 */
public record OnboardingAddressServiceRequest(
        Long memberId,
        Address address
) {
    public static OnboardingAddressServiceRequest of(
            Long memberId,
            String alias,
            String lotNumberAddress,
            String streetNameAddress,
            String detailedAddress,
            Double latitude,
            Double longitude,
            String addressType
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
                address
        );
    }
}
