package com.stdev.smartmealtable.admin.config;

import com.stdev.smartmealtable.domain.map.AddressSearchResult;
import com.stdev.smartmealtable.domain.map.MapService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.util.List;

/**
 * Admin 모듈 테스트를 위한 설정 클래스
 * 
 * <p>MapService Mock 빈을 제공하여 테스트 환경에서 지오코딩 API 호출 없이 테스트 가능하도록 합니다.</p>
 */
@TestConfiguration
public class AdminTestConfiguration {

    /**
     * 테스트용 MapService Mock 빈
     * 
     * <p>서울시 강남구 테헤란로의 좌표를 고정값으로 반환합니다.</p>
     */
    @Bean
    @Primary
    public MapService testMapService() {
        return new MapService() {
            @Override
            public List<AddressSearchResult> searchAddress(String keyword, Integer limit) {
                // 테스트용 고정 좌표 반환
                AddressSearchResult mockResult = new AddressSearchResult(
                        "서울시 강남구 테헤란로 123",   // roadAddress
                        "서울시 강남구 역삼동 456",     // jibunAddress
                        new BigDecimal("37.4979"),     // latitude
                        new BigDecimal("127.0276"),    // longitude
                        "서울특별시",                   // sido
                        "강남구",                       // sigungu
                        "역삼동",                       // dong
                        null,                           // buildingName
                        null,                           // sigunguCode
                        null                            // bcode
                );
                
                return List.of(mockResult);
            }

            @Override
            public AddressSearchResult reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
                // 역지오코딩 테스트용
                return new AddressSearchResult(
                        "서울시 강남구 테헤란로 123",   // roadAddress
                        "서울시 강남구 역삼동 456",     // jibunAddress
                        latitude,                       // latitude
                        longitude,                      // longitude
                        "서울특별시",                   // sido
                        "강남구",                       // sigungu
                        "역삼동",                       // dong
                        null,                           // buildingName
                        null,                           // sigunguCode
                        null                            // bcode
                );
            }
        };
    }
}
