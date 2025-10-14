package com.stdev.smartmealtable.api.map.dto;

import com.stdev.smartmealtable.domain.map.AddressSearchResult;

import java.math.BigDecimal;

/**
 * 주소 검색 결과 응답 DTO
 */
public record AddressSearchResultResponse(
        String roadAddress,
        String jibunAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        String sido,
        String sigungu,
        String dong,
        String buildingName,
        String sigunguCode,
        String bcode
) {
    public static AddressSearchResultResponse from(AddressSearchResult result) {
        return new AddressSearchResultResponse(
                result.roadAddress(),
                result.jibunAddress(),
                result.latitude(),
                result.longitude(),
                result.sido(),
                result.sigungu(),
                result.dong(),
                result.buildingName(),
                result.sigunguCode(),
                result.bcode()
        );
    }
}
