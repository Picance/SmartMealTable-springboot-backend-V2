package com.stdev.smartmealtable.api.map.dto;

import java.util.List;

/**
 * 주소 검색 응답 DTO (목록)
 */
public record AddressSearchServiceResponse(
        List<AddressSearchResultResponse> addresses,
        int totalCount
) {
    public static AddressSearchServiceResponse of(List<AddressSearchResultResponse> addresses) {
        return new AddressSearchServiceResponse(addresses, addresses.size());
    }
}
