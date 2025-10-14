package com.stdev.smartmealtable.client.external.naver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * 네이버 지도 API Geocoding 응답 DTO
 */
public record NaverGeocodingResponse(
        String status,
        @JsonProperty("meta") Meta meta,
        @JsonProperty("addresses") List<Address> addresses
) {
    public record Meta(
            @JsonProperty("totalCount") int totalCount,
            @JsonProperty("page") int page,
            @JsonProperty("count") int count
    ) {}
    
    public record Address(
            @JsonProperty("roadAddress") String roadAddress,
            @JsonProperty("jibunAddress") String jibunAddress,
            @JsonProperty("englishAddress") String englishAddress,
            @JsonProperty("x") String x,  // 경도
            @JsonProperty("y") String y,  // 위도
            @JsonProperty("distance") BigDecimal distance,
            @JsonProperty("addressElements") List<AddressElement> addressElements
    ) {}
    
    public record AddressElement(
            @JsonProperty("types") List<String> types,
            @JsonProperty("longName") String longName,
            @JsonProperty("shortName") String shortName,
            @JsonProperty("code") String code
    ) {}
}
