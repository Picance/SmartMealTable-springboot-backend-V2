package com.stdev.smartmealtable.api.food.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.food.service.FoodAutocompleteService;
import com.stdev.smartmealtable.api.food.service.dto.FoodAutocompleteResponse;
import com.stdev.smartmealtable.api.food.service.dto.FoodAutocompleteResponse.FoodSuggestion;
import com.stdev.smartmealtable.api.food.service.dto.FoodTrendingKeywordsResponse;
import com.stdev.smartmealtable.api.food.service.dto.FoodTrendingKeywordsResponse.TrendingKeyword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

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
 * FoodController REST Docs 테스트
 * - 음식 자동완성 & 인기 검색어 API 문서화
 */
@DisplayName("FoodController REST Docs - 검색 API")
class FoodControllerRestDocsTest extends AbstractRestDocsTest {

    @MockBean
    private FoodAutocompleteService foodAutocompleteService;

    @Test
    @DisplayName("음식 자동완성 성공 - 200")
    void autocomplete_success_docs() throws Exception {
        // given
        String keyword = "떡볶";
        int limit = 5;
        FoodAutocompleteResponse response = new FoodAutocompleteResponse(List.of(
                new FoodSuggestion(101L, "로제 떡볶이", 11L, "스마트분식", "분식", 12000, true),
                new FoodSuggestion(102L, "치즈 떡볶이", 12L, "맛동산", "분식", 10000, false)
        ));

        given(foodAutocompleteService.autocomplete(keyword, limit)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/foods/autocomplete")
                        .param("keyword", keyword)
                        .param("limit", String.valueOf(limit)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.suggestions").isArray())
                .andDo(document("food/autocomplete-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("자동완성 검색 키워드 (1~50자, 필수)"),
                                parameterWithName("limit").optional().description("조회할 결과 개수 (기본값: 10, 최대: 20)")
                        ),
                        responseFields(getAutocompleteSuccessResponseFields())
                ));
    }

    @Test
    @DisplayName("음식 자동완성 실패 - limit 범위 초과 (400)")
    void autocomplete_invalidLimit_docs() throws Exception {
        mockMvc.perform(get("/api/v1/foods/autocomplete")
                        .param("keyword", "떡볶")
                        .param("limit", "30"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E400"))
                .andDo(document("food/autocomplete-invalid-limit",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("자동완성 검색 키워드 (필수)"),
                                parameterWithName("limit").description("허용 범위를 초과한 결과 개수 (최대 20)")
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }

    @Test
    @DisplayName("인기 검색어 조회 성공 - 200")
    void trendingKeywords_success_docs() throws Exception {
        int limit = 3;
        FoodTrendingKeywordsResponse response = new FoodTrendingKeywordsResponse(List.of(
                new TrendingKeyword("치킨", 1523L, 1),
                new TrendingKeyword("떡볶이", 1204L, 2),
                new TrendingKeyword("마라탕", 1099L, 3)
        ));

        given(foodAutocompleteService.getTrendingKeywords(limit)).willReturn(response);

        mockMvc.perform(get("/api/v1/foods/trending")
                        .param("limit", String.valueOf(limit)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.keywords").isArray())
                .andDo(document("food/trending-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("limit").optional().description("조회할 인기 검색어 개수 (기본값: 10, 최대: 20)")
                        ),
                        responseFields(getTrendingSuccessResponseFields())
                ));
    }

    @Test
    @DisplayName("인기 검색어 조회 실패 - limit 최소값 미만 (400)")
    void trendingKeywords_invalidLimit_docs() throws Exception {
        mockMvc.perform(get("/api/v1/foods/trending")
                        .param("limit", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E400"))
                .andDo(document("food/trending-invalid-limit",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("limit").description("허용 범위 미만의 인기 검색어 개수 (최소 1)")
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }

    private FieldDescriptor[] getAutocompleteSuccessResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                fieldWithPath("data").type(JsonFieldType.OBJECT).description("자동완성 응답 데이터"),
                fieldWithPath("data.suggestions").type(JsonFieldType.ARRAY).description("자동완성 제안 목록"),
                fieldWithPath("data.suggestions[].foodId").type(JsonFieldType.NUMBER).description("음식 ID"),
                fieldWithPath("data.suggestions[].foodName").type(JsonFieldType.STRING).description("음식 이름"),
                fieldWithPath("data.suggestions[].storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                fieldWithPath("data.suggestions[].storeName").type(JsonFieldType.STRING).description("가게 이름"),
                fieldWithPath("data.suggestions[].categoryName").type(JsonFieldType.STRING).description("카테고리 이름"),
                fieldWithPath("data.suggestions[].averagePrice").type(JsonFieldType.NUMBER).description("평균 가격 (원)").optional(),
                fieldWithPath("data.suggestions[].isMain").type(JsonFieldType.BOOLEAN).description("대표 메뉴 여부"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
        };
    }

    private FieldDescriptor[] getTrendingSuccessResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                fieldWithPath("data").type(JsonFieldType.OBJECT).description("인기 검색어 응답 데이터"),
                fieldWithPath("data.keywords").type(JsonFieldType.ARRAY).description("인기 검색어 목록"),
                fieldWithPath("data.keywords[].keyword").type(JsonFieldType.STRING).description("검색어"),
                fieldWithPath("data.keywords[].searchCount").type(JsonFieldType.NUMBER).description("검색 횟수"),
                fieldWithPath("data.keywords[].rank").type(JsonFieldType.NUMBER).description("순위"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
        };
    }

    private FieldDescriptor[] getErrorResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (ERROR)"),
                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E400 등)"),
                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                fieldWithPath("error.data").type(JsonFieldType.OBJECT).description("에러 상세 정보 (필드 에러 등)").optional()
        };
    }
}
