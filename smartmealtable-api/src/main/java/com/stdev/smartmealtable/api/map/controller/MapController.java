package com.stdev.smartmealtable.api.map.controller;

import com.stdev.smartmealtable.api.map.dto.AddressSearchServiceResponse;
import com.stdev.smartmealtable.api.map.dto.ReverseGeocodeServiceResponse;
import com.stdev.smartmealtable.api.map.service.MapApplicationService;
import com.stdev.smartmealtable.core.response.ApiResponse;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 지도 및 위치 Controller
 * 
 * <p>주소 검색(Geocoding)과 좌표→주소 변환(Reverse Geocoding) API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/maps")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MapController {

    private final MapApplicationService mapApplicationService;

    /**
     * 주소 검색 (Geocoding)
     * 
     * @param keyword 검색 키워드 (도로명, 지번 등)
     * @param limit 결과 개수 제한 (기본값: 10)
     * @return 주소 검색 결과 목록
     */
    @GetMapping("/search-address")
    public ApiResponse<AddressSearchServiceResponse> searchAddress(
            @RequestParam @NotBlank(message = "검색 키워드는 필수입니다.") String keyword,
            @RequestParam(required = false, defaultValue = "10") 
            @Min(value = 1, message = "결과 개수는 최소 1개 이상이어야 합니다.")
            @Max(value = 100, message = "결과 개수는 최대 100개까지 조회 가능합니다.")
            Integer limit
    ) {
        log.info("GET /api/v1/maps/search-address - keyword: {}, limit: {}", keyword, limit);
        
        AddressSearchServiceResponse response = mapApplicationService.searchAddress(keyword, limit);
        
        return ApiResponse.success(response);
    }

    /**
     * 좌표 → 주소 변환 (Reverse Geocoding)
     * 
     * @param lat 위도 (-90 ~ 90)
     * @param lng 경도 (-180 ~ 180)
     * @return 주소 정보
     */
    @GetMapping("/reverse-geocode")
    public ApiResponse<ReverseGeocodeServiceResponse> reverseGeocode(
            @RequestParam @NotNull(message = "위도는 필수입니다.") 
            @DecimalMin(value = "-90.0", message = "위도는 -90 ~ 90 범위여야 합니다.")
            @DecimalMax(value = "90.0", message = "위도는 -90 ~ 90 범위여야 합니다.")
            BigDecimal lat,
            @RequestParam @NotNull(message = "경도는 필수입니다.")
            @DecimalMin(value = "-180.0", message = "경도는 -180 ~ 180 범위여야 합니다.")
            @DecimalMax(value = "180.0", message = "경도는 -180 ~ 180 범위여야 합니다.")
            BigDecimal lng
    ) {
        log.info("GET /api/v1/maps/reverse-geocode - lat: {}, lng: {}", lat, lng);
        
        ReverseGeocodeServiceResponse response = mapApplicationService.reverseGeocode(lat, lng);
        
        return ApiResponse.success(response);
    }
}
