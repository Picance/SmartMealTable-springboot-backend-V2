package com.stdev.smartmealtable.api.onboarding.dto.response;

/**
 * 온보딩 - 주소 등록 응답 DTO
 */
public record OnboardingAddressResponse(
        Long addressHistoryId,
        String alias,
        String lotNumberAddress,
        String streetNameAddress,
        String detailedAddress,
        Double latitude,
        Double longitude,
        String addressType,
        Boolean isPrimary
) {
}
