package com.stdev.smartmealtable.domain.map;

import java.math.BigDecimal;
import java.util.List;

/**
 * 지도 및 위치 도메인 서비스 인터페이스
 * 
 * <p>주소 검색(Geocoding)과 좌표→주소 변환(Reverse Geocoding) 기능을 제공합니다.</p>
 * <p>실제 구현체는 client 모듈의 NaverMapClient에서 네이버 지도 API를 호출하여 처리합니다.</p>
 */
public interface MapService {
    
    /**
     * 키워드로 주소를 검색합니다 (Geocoding)
     * 
     * <p>도로명, 지번 등의 키워드로 주소를 검색하고 결과 목록을 반환합니다.</p>
     * 
     * @param keyword 검색 키워드 (도로명, 지번 등)
     * @param limit 결과 개수 제한 (기본값: 10)
     * @return 주소 검색 결과 목록
     * @throws IllegalArgumentException 키워드가 null이거나 빈 문자열인 경우
     */
    List<AddressSearchResult> searchAddress(String keyword, Integer limit);
    
    /**
     * GPS 좌표를 주소로 변환합니다 (Reverse Geocoding)
     * 
     * <p>위도와 경도를 입력받아 해당 위치의 주소 정보를 반환합니다.</p>
     * <p>주소 등록 시 '현재 위치로 찾기' 기능에서 사용됩니다. (REQ-ONBOARD-203b)</p>
     * 
     * @param latitude 위도 (-90 ~ 90)
     * @param longitude 경도 (-180 ~ 180)
     * @return 주소 검색 결과
     * @throws IllegalArgumentException 좌표가 유효하지 않은 경우
     */
    AddressSearchResult reverseGeocode(BigDecimal latitude, BigDecimal longitude);
}
