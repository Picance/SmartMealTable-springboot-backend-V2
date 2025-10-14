package com.stdev.smartmealtable.api.map.service;

import com.stdev.smartmealtable.api.map.dto.AddressSearchServiceResponse;
import com.stdev.smartmealtable.api.map.dto.ReverseGeocodeServiceResponse;
import com.stdev.smartmealtable.domain.map.AddressSearchResult;
import com.stdev.smartmealtable.domain.map.MapService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * MapApplicationService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class MapApplicationServiceTest {

    @Mock
    private MapService mapService;

    @InjectMocks
    private MapApplicationService mapApplicationService;

    @Test
    @DisplayName("주소 검색 성공 - 복수 결과")
    void searchAddress_Success_MultipleResults() {
        // given
        String keyword = "테헤란로";
        Integer limit = 10;

        AddressSearchResult result1 = new AddressSearchResult(
                "서울특별시 강남구 테헤란로 427",
                "서울특별시 강남구 삼성동 143-37",
                new BigDecimal("37.5081"),
                new BigDecimal("127.0630"),
                "서울특별시",
                "강남구",
                "삼성동",
                "위워크 삼성역점",
                "11680",
                "1168010700"
        );

        AddressSearchResult result2 = new AddressSearchResult(
                "서울특별시 강남구 테헤란로 152",
                "서울특별시 강남구 역삼동 823-20",
                new BigDecimal("37.5005"),
                new BigDecimal("127.0355"),
                "서울특별시",
                "강남구",
                "역삼동",
                "강남파이낸스센터",
                "11680",
                "1168010300"
        );

        given(mapService.searchAddress(keyword, limit))
                .willReturn(List.of(result1, result2));

        // when
        AddressSearchServiceResponse response = mapApplicationService.searchAddress(keyword, limit);

        // then
        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.addresses()).hasSize(2);
        assertThat(response.addresses().get(0).roadAddress()).isEqualTo("서울특별시 강남구 테헤란로 427");
        assertThat(response.addresses().get(1).roadAddress()).isEqualTo("서울특별시 강남구 테헤란로 152");

        verify(mapService).searchAddress(keyword, limit);
    }

    @Test
    @DisplayName("주소 검색 성공 - 단일 결과")
    void searchAddress_Success_SingleResult() {
        // given
        String keyword = "위워크 삼성역점";
        Integer limit = 1;

        AddressSearchResult result = new AddressSearchResult(
                "서울특별시 강남구 테헤란로 427",
                "서울특별시 강남구 삼성동 143-37",
                new BigDecimal("37.5081"),
                new BigDecimal("127.0630"),
                "서울특별시",
                "강남구",
                "삼성동",
                "위워크 삼성역점",
                "11680",
                "1168010700"
        );

        given(mapService.searchAddress(keyword, limit))
                .willReturn(List.of(result));

        // when
        AddressSearchServiceResponse response = mapApplicationService.searchAddress(keyword, limit);

        // then
        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.addresses()).hasSize(1);
        assertThat(response.addresses().get(0).buildingName()).isEqualTo("위워크 삼성역점");

        verify(mapService).searchAddress(keyword, limit);
    }

    @Test
    @DisplayName("주소 검색 성공 - 결과 없음")
    void searchAddress_Success_NoResults() {
        // given
        String keyword = "존재하지않는주소";
        Integer limit = 10;

        given(mapService.searchAddress(keyword, limit))
                .willReturn(List.of());

        // when
        AddressSearchServiceResponse response = mapApplicationService.searchAddress(keyword, limit);

        // then
        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isZero();
        assertThat(response.addresses()).isEmpty();

        verify(mapService).searchAddress(keyword, limit);
    }

    @Test
    @DisplayName("역지오코딩 성공")
    void reverseGeocode_Success() {
        // given
        BigDecimal lat = new BigDecimal("37.5081");
        BigDecimal lng = new BigDecimal("127.0630");

        AddressSearchResult result = new AddressSearchResult(
                "서울특별시 강남구 테헤란로 427",
                "서울특별시 강남구 삼성동 143-37",
                lat,
                lng,
                "서울특별시",
                "강남구",
                "삼성동",
                "위워크 삼성역점",
                "11680",
                "1168010700"
        );

        given(mapService.reverseGeocode(lat, lng))
                .willReturn(result);

        // when
        ReverseGeocodeServiceResponse response = mapApplicationService.reverseGeocode(lat, lng);

        // then
        assertThat(response).isNotNull();
        assertThat(response.roadAddress()).isEqualTo("서울특별시 강남구 테헤란로 427");
        assertThat(response.jibunAddress()).isEqualTo("서울특별시 강남구 삼성동 143-37");
        assertThat(response.sido()).isEqualTo("서울특별시");
        assertThat(response.sigungu()).isEqualTo("강남구");
        assertThat(response.dong()).isEqualTo("삼성동");
        assertThat(response.buildingName()).isEqualTo("위워크 삼성역점");
        assertThat(response.latitude()).isEqualByComparingTo(lat);
        assertThat(response.longitude()).isEqualByComparingTo(lng);

        verify(mapService).reverseGeocode(lat, lng);
    }

    @Test
    @DisplayName("역지오코딩 성공 - 건물명 없음")
    void reverseGeocode_Success_NoBuildingName() {
        // given
        BigDecimal lat = new BigDecimal("37.5081");
        BigDecimal lng = new BigDecimal("127.0630");

        AddressSearchResult result = new AddressSearchResult(
                "서울특별시 강남구 테헤란로 427",
                "서울특별시 강남구 삼성동 143-37",
                lat,
                lng,
                "서울특별시",
                "강남구",
                "삼성동",
                null,  // 건물명 없음
                "11680",
                "1168010700"
        );

        given(mapService.reverseGeocode(lat, lng))
                .willReturn(result);

        // when
        ReverseGeocodeServiceResponse response = mapApplicationService.reverseGeocode(lat, lng);

        // then
        assertThat(response).isNotNull();
        assertThat(response.roadAddress()).isEqualTo("서울특별시 강남구 테헤란로 427");
        assertThat(response.buildingName()).isNull();

        verify(mapService).reverseGeocode(lat, lng);
    }
}
