package com.stdev.smartmealtable.client.external.naver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 네이버 지도 API Reverse Geocoding 응답 DTO
 */
public record NaverReverseGeocodingResponse(
        String status,
        @JsonProperty("results") List<Result> results
) {
    public record Result(
            @JsonProperty("name") String name,
            @JsonProperty("code") Code code,
            @JsonProperty("region") Region region,
            @JsonProperty("land") Land land
    ) {}
    
    public record Code(
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("mappingId") String mappingId
    ) {}
    
    public record Region(
            @JsonProperty("area0") Area area0,  // 국가
            @JsonProperty("area1") Area area1,  // 시/도
            @JsonProperty("area2") Area area2,  // 시/군/구
            @JsonProperty("area3") Area area3,  // 읍/면/동
            @JsonProperty("area4") Area area4   // 리
    ) {}
    
    public record Area(
            @JsonProperty("name") String name,
            @JsonProperty("coords") Coords coords,
            @JsonProperty("alias") String alias
    ) {}
    
    public record Coords(
            @JsonProperty("center") Center center
    ) {}
    
    public record Center(
            @JsonProperty("crs") String crs,
            @JsonProperty("x") String x,
            @JsonProperty("y") String y
    ) {}
    
    public record Land(
            @JsonProperty("type") String type,
            @JsonProperty("number1") String number1,
            @JsonProperty("number2") String number2,
            @JsonProperty("addition0") Addition addition0,
            @JsonProperty("addition1") Addition addition1,
            @JsonProperty("addition2") Addition addition2,
            @JsonProperty("addition3") Addition addition3,
            @JsonProperty("addition4") Addition addition4,
            @JsonProperty("name") String name,
            @JsonProperty("coords") Coords coords
    ) {}
    
    public record Addition(
            @JsonProperty("type") String type,
            @JsonProperty("value") String value
    ) {}
}
