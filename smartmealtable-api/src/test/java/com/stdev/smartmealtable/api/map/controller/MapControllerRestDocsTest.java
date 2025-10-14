package com.stdev.smartmealtable.api.map.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.map.dto.AddressSearchResultResponse;
import com.stdev.smartmealtable.api.map.dto.AddressSearchServiceResponse;
import com.stdev.smartmealtable.api.map.dto.ReverseGeocodeServiceResponse;
import com.stdev.smartmealtable.api.map.service.MapApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 지도 및 위치 API REST Docs 테스트
 */
@Transactional
class MapControllerRestDocsTest extends AbstractRestDocsTest {

    @MockBean
    private MapApplicationService mapApplicationService;

    @Test
    @DisplayName("[Docs] 주소 검색 성공")
    void searchAddress_Success_Docs() throws Exception {
        // given
        String keyword = "테헤란로";
        Integer limit = 10;

        AddressSearchResultResponse result1 = new AddressSearchResultResponse(
                "서울특별시 강남구 테헤란로 427",
                "서울특별시 강남구 삼성동 143-37",
                new BigDecimal("37.5081"),
                new BigDecimal("127.0630"),
                "위워크 삼성역점",
                "11680",
                "1168010700"
        );

        AddressSearchResultResponse result2 = new AddressSearchResultResponse(
                "서울특별시 강남구 테헤란로 152",
                "서울특별시 강남구 역삼동 823-20",
                new BigDecimal("37.5005"),
                new BigDecimal("127.0355"),
                "강남파이낸스센터",
                "11680",
                "1168010300"
        );

        AddressSearchServiceResponse response = AddressSearchServiceResponse.of(
                List.of(result1, result2)
        );

        given(mapApplicationService.searchAddress(eq(keyword), eq(limit)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/maps/search-address")
                        .queryParam("keyword", keyword)
                        .queryParam("limit", String.valueOf(limit)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.addresses").isArray())
                .andExpect(jsonPath("$.data.addresses[0].roadAddress").value("서울특별시 강남구 테헤란로 427"))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andDo(document("map/search-address",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드 (도로명, 지번 등)"),
                                parameterWithName("limit").description("결과 개수 제한 (기본값: 10, 최소: 1, 최대: 100)").optional()
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.STRING).description("응답 상태 (success/error)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.addresses").type(JsonFieldType.ARRAY).description("주소 검색 결과 목록"),
                                fieldWithPath("data.addresses[].roadAddress").type(JsonFieldType.STRING).description("도로명 주소"),
                                fieldWithPath("data.addresses[].jibunAddress").type(JsonFieldType.STRING).description("지번 주소"),
                                fieldWithPath("data.addresses[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data.addresses[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data.addresses[].buildingName").type(JsonFieldType.STRING).description("건물명").optional(),
                                fieldWithPath("data.addresses[].sigunguCode").type(JsonFieldType.STRING).description("시군구 코드"),
                                fieldWithPath("data.addresses[].bcode").type(JsonFieldType.STRING).description("법정동 코드"),
                                fieldWithPath("data.totalCount").type(JsonFieldType.NUMBER).description("전체 결과 개수"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 주소 검색 실패 - 키워드 누락")
    void searchAddress_Fail_MissingKeyword_Docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/maps/search-address")
                        .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andDo(document("map/search-address-fail-missing-keyword",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.STRING).description("응답 상태 (error)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 역지오코딩 성공")
    void reverseGeocode_Success_Docs() throws Exception {
        // given
        BigDecimal lat = new BigDecimal("37.5081");
        BigDecimal lng = new BigDecimal("127.0630");

        ReverseGeocodeServiceResponse response = new ReverseGeocodeServiceResponse(
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

        given(mapApplicationService.reverseGeocode(eq(lat), eq(lng)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/maps/reverse-geocode")
                        .queryParam("lat", lat.toString())
                        .queryParam("lng", lng.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.roadAddress").value("서울특별시 강남구 테헤란로 427"))
                .andExpect(jsonPath("$.data.sido").value("서울특별시"))
                .andDo(document("map/reverse-geocode",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("lat").description("위도 (-90 ~ 90)"),
                                parameterWithName("lng").description("경도 (-180 ~ 180)")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.STRING).description("응답 상태 (success/error)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.roadAddress").type(JsonFieldType.STRING).description("도로명 주소"),
                                fieldWithPath("data.jibunAddress").type(JsonFieldType.STRING).description("지번 주소"),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data.sido").type(JsonFieldType.STRING).description("시/도"),
                                fieldWithPath("data.sigungu").type(JsonFieldType.STRING).description("시/군/구"),
                                fieldWithPath("data.dong").type(JsonFieldType.STRING).description("읍/면/동"),
                                fieldWithPath("data.buildingName").type(JsonFieldType.STRING).description("건물명").optional(),
                                fieldWithPath("data.sigunguCode").type(JsonFieldType.STRING).description("시군구 코드"),
                                fieldWithPath("data.bcode").type(JsonFieldType.STRING).description("법정동 코드"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 역지오코딩 실패 - 유효하지 않은 위도")
    void reverseGeocode_Fail_InvalidLatitude_Docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/maps/reverse-geocode")
                        .queryParam("lat", "100.0")  // 위도 범위 초과
                        .queryParam("lng", "127.0630"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andDo(document("map/reverse-geocode-fail-invalid-latitude",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.STRING).description("응답 상태 (error)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 역지오코딩 실패 - 경도 누락")
    void reverseGeocode_Fail_MissingLongitude_Docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/maps/reverse-geocode")
                        .queryParam("lat", "37.5081"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andDo(document("map/reverse-geocode-fail-missing-longitude",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.STRING).description("응답 상태 (error)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }
}
