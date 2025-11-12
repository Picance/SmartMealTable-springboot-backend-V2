package com.stdev.smartmealtable.api.recommendation.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 통합 자동완성 (음식 + 가게) API REST Docs 테스트
 * TDD: RED-GREEN-REFACTOR
 *
 * @author SmartMealTable Team
 * @since 2025-11-12
 */
@DisplayName("통합 자동완성 API REST Docs 테스트")
class AutocompleteControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        // AbstractRestDocsTest에서 Redis Mock 이미 설정됨

        // 카테고리 생성
        Category category = categoryRepository.save(Category.create("한식"));
        categoryId = category.getCategoryId();

        // 테스트 데이터 생성
        createTestData();
    }

    private void createTestData() {
        // 가게 데이터 생성
        Store store1 = storeRepository.save(createStore("교촌치킨 강남점", "서울특별시 강남구 테헤란로 123"));
        Store store2 = storeRepository.save(createStore("bhc 역삼점", "서울특별시 강남구 역삼로 100"));
        Store store3 = storeRepository.save(createStore("한식당", "서울특별시 강남구 강남대로 200"));
        Store store4 = storeRepository.save(createStore("김치나라", "서울특별시 강남구 강남대로 300"));

        // 음식 데이터 생성
        foodRepository.save(createFood("교촌 오리지널", store1.getStoreId(), 18000));
        foodRepository.save(createFood("레드콤보", store1.getStoreId(), 22000));
        foodRepository.save(createFood("bhc 순살", store2.getStoreId(), 20000));
        foodRepository.save(createFood("김치찜", store4.getStoreId(), 15000));
        foodRepository.save(createFood("김치전", store4.getStoreId(), 10000));
        foodRepository.save(createFood("김치국", store4.getStoreId(), 12000));
    }

    private Store createStore(String name, String address) {
        return Store.builder()
                .name(name)
                .categoryIds(java.util.List.of(categoryId))
                .sellerId(1L)
                .address(address)
                .lotNumberAddress(address)
                .latitude(new BigDecimal("37.5"))
                .longitude(new BigDecimal("127.0"))
                .phoneNumber("02-1234-5678")
                .description("테스트 가게")
                .averagePrice(15000)
                .reviewCount(100)
                .viewCount(1000)
                .favoriteCount(50)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://cdn.smartmealtable.com/stores/cover.jpg")
                .registeredAt(LocalDateTime.now())
                .build();
    }

    private Food createFood(String name, Long storeId, int price) {
        return Food.builder()
                .foodName(name)
                .storeId(storeId)
                .categoryId(categoryId)
                .description("테스트 음식")
                .price(price)
                .displayOrder(1)
                .isMain(true)
                .imageUrl("https://cdn.smartmealtable.com/foods/food.jpg")
                .registeredDt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("통합 자동완성 성공 - 기본 파라미터만 사용")
    void autocomplete_Success_WithKeywordOnly() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/autocomplete")
                        .param("keyword", "김치"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.suggestions").isArray())
                .andExpect(jsonPath("$.data.suggestions.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.storeShortcuts").isArray())
                .andDo(document("autocomplete-success-basic",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드 (필수, 1-50자)"),
                                parameterWithName("limit").description("결과 개수 제한 (선택, 기본값: 10, 최소 1, 최대 20)").optional(),
                                parameterWithName("storeShortcutsLimit").description("가게 바로가기 개수 제한 (선택, 기본값: 10, 최소 1, 최대 20)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data.suggestions[]").description("자동완성 키워드 목록 (음식명 + 가게명 혼합, 최대 limit개)"),
                                fieldWithPath("data.storeShortcuts[]").description("가게 바로가기 목록 (최대 storeShortcutsLimit개)"),
                                fieldWithPath("data.storeShortcuts[].storeId").description("가게 ID"),
                                fieldWithPath("data.storeShortcuts[].name").description("가게 이름"),
                                fieldWithPath("data.storeShortcuts[].imageUrl").description("대표 이미지 URL"),
                                fieldWithPath("data.storeShortcuts[].isOpen").description("영업 중 여부"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("통합 자동완성 성공 - 모든 파라미터 지정")
    void autocomplete_Success_WithAllParameters() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/autocomplete")
                        .param("keyword", "교촌")
                        .param("limit", "5")
                        .param("storeShortcutsLimit", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.suggestions").isArray())
                .andExpect(jsonPath("$.data.suggestions.length()").value(lessThanOrEqualTo(5)))
                .andExpect(jsonPath("$.data.storeShortcuts").isArray())
                .andExpect(jsonPath("$.data.storeShortcuts.length()").value(lessThanOrEqualTo(3)))
                .andDo(document("autocomplete-success-with-all-params",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드 (필수, 1-50자)"),
                                parameterWithName("limit").description("결과 개수 제한 (선택, 기본값: 10, 최소 1, 최대 20)"),
                                parameterWithName("storeShortcutsLimit").description("가게 바로가기 개수 제한 (선택, 기본값: 10, 최소 1, 최대 20)")
                        ),
                        responseFields(
                                fieldWithPath("result").description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data.suggestions[]").description("자동완성 키워드 목록 (음식명 + 가게명 혼합)"),
                                fieldWithPath("data.storeShortcuts[]").description("가게 바로가기 목록"),
                                fieldWithPath("data.storeShortcuts[].storeId").description("가게 ID"),
                                fieldWithPath("data.storeShortcuts[].name").description("가게 이름"),
                                fieldWithPath("data.storeShortcuts[].imageUrl").description("대표 이미지 URL"),
                                fieldWithPath("data.storeShortcuts[].isOpen").description("영업 중 여부"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("통합 자동완성 성공 - 결과 없음")
    void autocomplete_Success_NoResult() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/autocomplete")
                        .param("keyword", "존재하지않는음식"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.suggestions").isArray())
                .andExpect(jsonPath("$.data.suggestions.length()").value(0))
                .andExpect(jsonPath("$.data.storeShortcuts").isArray())
                .andExpect(jsonPath("$.data.storeShortcuts.length()").value(0));
    }

    @Test
    @DisplayName("통합 자동완성 성공 - storeShortcutsLimit 제한")
    void autocomplete_Success_StoreShortcutsLimited() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/autocomplete")
                        .param("keyword", "김치")
                        .param("storeShortcutsLimit", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeShortcuts").isArray())
                .andExpect(jsonPath("$.data.storeShortcuts.length()").value(lessThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("통합 자동완성 실패 - 키워드 없음 (400)")
    void autocomplete_Fail_NoKeyword() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/autocomplete"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통합 자동완성 실패 - 빈 키워드 (400)")
    void autocomplete_Fail_EmptyKeyword() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/autocomplete")
                        .param("keyword", ""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통합 자동완성 실패 - 키워드 길이 초과 (400)")
    void autocomplete_Fail_KeywordTooLong() throws Exception {
        // when & then
        String longKeyword = "a".repeat(51);
        mockMvc.perform(get("/api/v1/autocomplete")
                        .param("keyword", longKeyword))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통합 자동완성 실패 - limit 음수 (400)")
    void autocomplete_Fail_NegativeLimit() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/autocomplete")
                        .param("keyword", "김치")
                        .param("limit", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통합 자동완성 실패 - limit 최대값 초과 (400)")
    void autocomplete_Fail_LimitTooLarge() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/autocomplete")
                        .param("keyword", "김치")
                        .param("limit", "100"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통합 자동완성 실패 - storeShortcutsLimit 음수 (400)")
    void autocomplete_Fail_NegativeStoreShortcutsLimit() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/autocomplete")
                        .param("keyword", "김치")
                        .param("storeShortcutsLimit", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통합 자동완성 실패 - storeShortcutsLimit 최대값 초과 (400)")
    void autocomplete_Fail_StoreShortcutsLimitTooLarge() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/autocomplete")
                        .param("keyword", "김치")
                        .param("storeShortcutsLimit", "100"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
