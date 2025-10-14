package com.stdev.smartmealtable.api.map.service;

import com.stdev.smartmealtable.api.map.dto.AddressSearchResultResponse;
import com.stdev.smartmealtable.api.map.dto.AddressSearchServiceResponse;
import com.stdev.smartmealtable.api.map.dto.ReverseGeocodeServiceResponse;
import com.stdev.smartmealtable.domain.map.AddressSearchResult;
import com.stdev.smartmealtable.domain.map.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지도 및 위치 Application Service
 * 
 * <p>주소 검색 및 좌표 변환 기능을 제공합니다.</p>
 * <p>MapService(NaverMapClient)를 호출하여 네이버 지도 API를 활용합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MapApplicationService {

    private final MapService mapService;

    /**
     * 키워드로 주소 검색 (Geocoding)
     * 
     * @param keyword 검색 키워드
     * @param limit 결과 개수 제한 (기본값: 10)
     * @return 주소 검색 결과 목록
     */
    public AddressSearchServiceResponse searchAddress(String keyword, Integer limit) {
        log.info("주소 검색 요청 - keyword: {}, limit: {}", keyword, limit);
        
        List<AddressSearchResult> results = mapService.searchAddress(keyword, limit);
        
        List<AddressSearchResultResponse> responses = results.stream()
                .map(AddressSearchResultResponse::from)
                .collect(Collectors.toList());
        
        log.info("주소 검색 완료 - 결과 개수: {}", responses.size());
        
        return AddressSearchServiceResponse.of(responses);
    }

    /**
     * GPS 좌표를 주소로 변환 (Reverse Geocoding)
     * 
     * @param latitude 위도
     * @param longitude 경도
     * @return 주소 정보
     */
    public ReverseGeocodeServiceResponse reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        log.info("Reverse Geocoding 요청 - lat: {}, lng: {}", latitude, longitude);
        
        AddressSearchResult result = mapService.reverseGeocode(latitude, longitude);
        
        log.info("Reverse Geocoding 완료 - roadAddress: {}", result.roadAddress());
        
        return ReverseGeocodeServiceResponse.from(result);
    }
}
