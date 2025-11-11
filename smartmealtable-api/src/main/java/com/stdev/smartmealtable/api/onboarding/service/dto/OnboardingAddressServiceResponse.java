package com.stdev.smartmealtable.api.onboarding.service.dto;

import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;

/**
 * 온보딩 - 주소 등록 Service Response DTO
 */
public record OnboardingAddressServiceResponse(
        Long addressHistoryId,
        Long memberId,
        String alias,
        String lotNumberAddress,
        String streetNameAddress,
        String detailedAddress,
        Double latitude,
        Double longitude,
        AddressType addressType,
        Boolean isPrimary
) {
    public static OnboardingAddressServiceResponse from(AddressHistory addressHistory) {
        return new OnboardingAddressServiceResponse(
                addressHistory.getAddressHistoryId(),
                addressHistory.getMemberId(),
                addressHistory.getAddress().getAlias(),
                addressHistory.getAddress().getLotNumberAddress(),
                addressHistory.getAddress().getStreetNameAddress(),
                addressHistory.getAddress().getDetailedAddress(),
                addressHistory.getAddress().getLatitude(),
                addressHistory.getAddress().getLongitude(),
                addressHistory.getAddress().getAddressType(),
                addressHistory.getIsPrimary()
        );
    }
}
