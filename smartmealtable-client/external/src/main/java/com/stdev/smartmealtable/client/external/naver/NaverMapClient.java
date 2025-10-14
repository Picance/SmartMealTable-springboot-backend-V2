package com.stdev.smartmealtable.client.external.naver;

import com.stdev.smartmealtable.client.external.naver.dto.NaverGeocodingResponse;
import com.stdev.smartmealtable.client.external.naver.dto.NaverReverseGeocodingResponse;
import com.stdev.smartmealtable.domain.map.AddressSearchResult;
import com.stdev.smartmealtable.domain.map.MapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 네이버 지도 API 클라이언트
 * 
 * <p>네이버 지도 API를 호출하여 Geocoding 및 Reverse Geocoding 기능을 제공합니다.</p>
 * <p>MapService 인터페이스를 구현하여 도메인 계층에서 사용할 수 있도록 합니다.</p>
 */
@Component
@Slf4j
public class NaverMapClient implements MapService {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public NaverMapClient(
            @Value("${naver.map.client-id}") String clientId,
            @Value("${naver.map.client-secret}") String clientSecret
    ) {
        this.restClient = RestClient.create();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public List<AddressSearchResult> searchAddress(String keyword, Integer limit) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("검색 키워드는 필수입니다.");
        }
        
        int searchLimit = limit != null ? limit : 10;
        
        try {
            log.info("네이버 지도 API Geocoding 호출 - keyword: {}, limit: {}", keyword, searchLimit);
            
            NaverGeocodingResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("naveropenapi.apigw.ntruss.com")
                            .path("/map-geocode/v2/geocode")
                            .queryParam("query", keyword)
                            .queryParam("count", searchLimit)
                            .build())
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .retrieve()
                    .body(NaverGeocodingResponse.class);

            if (response == null || response.addresses() == null) {
                log.warn("네이버 지도 API 응답이 비어있습니다.");
                return Collections.emptyList();
            }

            return response.addresses().stream()
                    .map(this::convertToAddressSearchResult)
                    .collect(Collectors.toList());
                    
        } catch (RestClientException e) {
            log.error("네이버 지도 API Geocoding 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("주소 검색 서비스에 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }

    @Override
    public AddressSearchResult reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null) {
            throw new IllegalArgumentException("위도는 필수입니다.");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("경도는 필수입니다.");
        }
        
        try {
            log.info("네이버 지도 API Reverse Geocoding 호출 - lat: {}, lng: {}", latitude, longitude);
            
            NaverReverseGeocodingResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("naveropenapi.apigw.ntruss.com")
                            .path("/map-reversegeocode/v2/gc")
                            .queryParam("coords", longitude + "," + latitude)  // 경도,위도 순서
                            .queryParam("orders", "roadaddr,addr")
                            .queryParam("output", "json")
                            .build())
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .retrieve()
                    .body(NaverReverseGeocodingResponse.class);

            if (response == null || response.results() == null || response.results().isEmpty()) {
                log.warn("네이버 지도 API Reverse Geocoding 응답이 비어있습니다.");
                throw new RuntimeException("해당 좌표의 주소를 찾을 수 없습니다.");
            }

            return convertReverseGeocodingToAddressSearchResult(response.results().get(0), latitude, longitude);
            
        } catch (RestClientException e) {
            log.error("네이버 지도 API Reverse Geocoding 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("주소 변환 서비스에 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }

    /**
     * 네이버 Geocoding 응답을 AddressSearchResult로 변환
     */
    private AddressSearchResult convertToAddressSearchResult(NaverGeocodingResponse.Address address) {
        String sigunguCode = extractElementCode(address.addressElements(), "SIGUGUN");
        String bcode = extractElementCode(address.addressElements(), "RI");
        String sido = extractElementName(address.addressElements(), "SIDO");
        String sigungu = extractElementName(address.addressElements(), "SIGUGUN");
        String dong = extractElementName(address.addressElements(), "DONGMYUN");
        String buildingName = extractElementName(address.addressElements(), "BUILDING_NAME");
        
        return new AddressSearchResult(
                address.roadAddress() != null ? address.roadAddress() : "",
                address.jibunAddress(),
                new BigDecimal(address.y()),  // 위도
                new BigDecimal(address.x()),  // 경도
                sido,
                sigungu,
                dong,
                buildingName,
                sigunguCode,
                bcode
        );
    }

    /**
     * 네이버 Reverse Geocoding 응답을 AddressSearchResult로 변환
     */
    private AddressSearchResult convertReverseGeocodingToAddressSearchResult(
            NaverReverseGeocodingResponse.Result result,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        String sido = result.region() != null && result.region().area1() != null ? 
                result.region().area1().name() : "";
        String sigungu = result.region() != null && result.region().area2() != null ? 
                result.region().area2().name() : "";
        String dong = result.region() != null && result.region().area3() != null ? 
                result.region().area3().name() : "";
        
        // 도로명 주소 구성
        String roadAddress = buildRoadAddress(sido, sigungu, dong, result.land());
        
        // 지번 주소 구성
        String jibunAddress = buildJibunAddress(sido, sigungu, dong, result.land());
        
        return new AddressSearchResult(
                roadAddress,
                jibunAddress,
                latitude,
                longitude,
                sido,
                sigungu,
                dong,
                result.land() != null ? result.land().name() : null,
                result.code() != null ? result.code().id() : null,
                result.code() != null ? result.code().mappingId() : null
        );
    }

    /**
     * AddressElement에서 특정 타입의 코드 추출
     */
    private String extractElementCode(List<NaverGeocodingResponse.AddressElement> elements, String type) {
        if (elements == null) return null;
        
        return elements.stream()
                .filter(e -> e.types() != null && e.types().contains(type))
                .map(NaverGeocodingResponse.AddressElement::code)
                .findFirst()
                .orElse(null);
    }

    /**
     * AddressElement에서 특정 타입의 이름 추출
     */
    private String extractElementName(List<NaverGeocodingResponse.AddressElement> elements, String type) {
        if (elements == null) return null;
        
        return elements.stream()
                .filter(e -> e.types() != null && e.types().contains(type))
                .map(NaverGeocodingResponse.AddressElement::longName)
                .findFirst()
                .orElse(null);
    }

    /**
     * 도로명 주소 구성
     */
    private String buildRoadAddress(String sido, String sigungu, String dong, NaverReverseGeocodingResponse.Land land) {
        StringBuilder sb = new StringBuilder();
        if (sido != null && !sido.isEmpty()) sb.append(sido).append(" ");
        if (sigungu != null && !sigungu.isEmpty()) sb.append(sigungu).append(" ");
        if (land != null && land.name() != null && !land.name().isEmpty()) {
            sb.append(land.name());
        }
        return sb.toString().trim();
    }

    /**
     * 지번 주소 구성
     */
    private String buildJibunAddress(String sido, String sigungu, String dong, NaverReverseGeocodingResponse.Land land) {
        StringBuilder sb = new StringBuilder();
        if (sido != null && !sido.isEmpty()) sb.append(sido).append(" ");
        if (sigungu != null && !sigungu.isEmpty()) sb.append(sigungu).append(" ");
        if (dong != null && !dong.isEmpty()) sb.append(dong).append(" ");
        if (land != null) {
            if (land.number1() != null && !land.number1().isEmpty()) {
                sb.append(land.number1());
                if (land.number2() != null && !land.number2().isEmpty()) {
                    sb.append("-").append(land.number2());
                }
            }
        }
        return sb.toString().trim();
    }
}
