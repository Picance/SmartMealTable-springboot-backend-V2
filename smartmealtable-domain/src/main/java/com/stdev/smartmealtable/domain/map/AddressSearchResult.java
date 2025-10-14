package com.stdev.smartmealtable.domain.map;

import java.math.BigDecimal;

/**
 * 주소 검색 결과 Value Object
 * 
 * <p>네이버 지도 API의 Geocoding 및 Reverse Geocoding 결과를 나타내는 불변 객체입니다.</p>
 * 
 * @param roadAddress 도로명 주소
 * @param jibunAddress 지번 주소
 * @param latitude 위도 (-90 ~ 90)
 * @param longitude 경도 (-180 ~ 180)
 * @param sido 시/도 (예: 서울특별시)
 * @param sigungu 시/군/구 (예: 강남구)
 * @param dong 읍/면/동 (예: 역삼동)
 * @param buildingName 건물명
 * @param sigunguCode 시/군/구 코드
 * @param bcode 법정동 코드
 */
public record AddressSearchResult(
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
    /**
     * AddressSearchResult 생성자
     * 
     * <p>주소 검색 결과 Value Object를 생성합니다.</p>
     * 
     * @param roadAddress 도로명 주소 (필수)
     * @param jibunAddress 지번 주소 (선택)
     * @param latitude 위도 (필수, -90 ~ 90 범위)
     * @param longitude 경도 (필수, -180 ~ 180 범위)
     * @param sido 시/도 (선택)
     * @param sigungu 시/군/구 (선택)
     * @param dong 읍/면/동 (선택)
     * @param buildingName 건물명 (선택)
     * @param sigunguCode 시/군/구 코드 (선택)
     * @param bcode 법정동 코드 (선택)
     * @throws IllegalArgumentException 위도 또는 경도가 유효하지 않은 경우
     */
    public AddressSearchResult {
        if (roadAddress == null || roadAddress.isBlank()) {
            throw new IllegalArgumentException("도로명 주소는 필수입니다.");
        }
        if (latitude == null) {
            throw new IllegalArgumentException("위도는 필수입니다.");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("경도는 필수입니다.");
        }
        
        validateLatitude(latitude);
        validateLongitude(longitude);
    }
    
    /**
     * 위도 유효성 검증
     * 
     * @param latitude 위도
     * @throws IllegalArgumentException 위도가 -90 ~ 90 범위를 벗어난 경우
     */
    private static void validateLatitude(BigDecimal latitude) {
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || 
            latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new IllegalArgumentException("위도는 -90 ~ 90 범위여야 합니다.");
        }
    }
    
    /**
     * 경도 유효성 검증
     * 
     * @param longitude 경도
     * @throws IllegalArgumentException 경도가 -180 ~ 180 범위를 벗어난 경우
     */
    private static void validateLongitude(BigDecimal longitude) {
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || 
            longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("경도는 -180 ~ 180 범위여야 합니다.");
        }
    }
}
