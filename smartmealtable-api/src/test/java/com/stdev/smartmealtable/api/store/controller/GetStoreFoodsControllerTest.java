package com.stdev.smartmealtable.api.store.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreImage;
import com.stdev.smartmealtable.domain.store.StoreImageRepository;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 가게별 메뉴 목록 조회 API REST Docs 테스트
 */
@DisplayName("가게별 메뉴 목록 조회 API REST Docs 테스트")
@Transactional
class GetStoreFoodsControllerTest extends AbstractRestDocsTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreImageRepository storeImageRepository;

    @Autowired
    private FoodRepository foodRepository;

    private Store testStore;
    private Food mainFood1;
    private Food mainFood2;
    private Food regularFood1;
    private Food regularFood2;

    @BeforeEach
    void setUp() {
        // 테스트 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시");
        groupRepository.save(testGroup);

        // 테스트 가게 생성
        testStore = Store.builder()
                .name("테스트 식당")
                .categoryId(1L)
                .sellerId(1L)
                .address("서울특별시 강남구 테헤란로 123")
                .lotNumberAddress("서울특별시 강남구 역삼동 123-45")
                .latitude(new BigDecimal("37.5012345"))
                .longitude(new BigDecimal("127.0398765"))
                .phoneNumber("02-1234-5678")
                .description("맛있는 음식을 제공하는 식당")
                .averagePrice(10000)
                .reviewCount(100)
                .viewCount(500)
                .favoriteCount(20)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store.jpg")
                .registeredAt(LocalDateTime.now().minusMonths(1))
                .deletedAt(null)
                .build();
        testStore = storeRepository.save(testStore);

        // 가게 이미지 생성
        StoreImage mainImage = StoreImage.builder()
                .storeId(testStore.getStoreId())
                .imageUrl("https://example.com/main.jpg")
                .isMain(true)
                .displayOrder(1)
                .build();
        storeImageRepository.save(mainImage);

        StoreImage subImage1 = StoreImage.builder()
                .storeId(testStore.getStoreId())
                .imageUrl("https://example.com/sub1.jpg")
                .isMain(false)
                .displayOrder(2)
                .build();
        storeImageRepository.save(subImage1);

        // 테스트 메뉴 생성 (isMain, displayOrder 다양하게)
        mainFood1 = Food.builder()
                .foodName("대표메뉴 1 - 김치찌개")
                .storeId(testStore.getStoreId())
                .categoryId(1L)
                .description("시원한 김치찌개")
                .imageUrl("https://example.com/kimchi.jpg")
                .averagePrice(8000)
                .price(8000)
                .isMain(true)
                .displayOrder(1)
                .registeredDt(LocalDateTime.now().minusWeeks(2))
                .deletedAt(null)
                .build();
        mainFood1 = foodRepository.save(mainFood1);

        mainFood2 = Food.builder()
                .foodName("대표메뉴 2 - 된장찌개")
                .storeId(testStore.getStoreId())
                .categoryId(1L)
                .description("구수한 된장찌개")
                .imageUrl("https://example.com/doenjang.jpg")
                .averagePrice(7500)
                .price(7500)
                .isMain(true)
                .displayOrder(2)
                .registeredDt(LocalDateTime.now().minusWeeks(1))
                .deletedAt(null)
                .build();
        mainFood2 = foodRepository.save(mainFood2);

        regularFood1 = Food.builder()
                .foodName("일반메뉴 1 - 비빔밥")
                .storeId(testStore.getStoreId())
                .categoryId(1L)
                .description("영양만점 비빔밥")
                .imageUrl("https://example.com/bibimbap.jpg")
                .averagePrice(9000)
                .price(9000)
                .isMain(false)
                .displayOrder(3)
                .registeredDt(LocalDateTime.now().minusDays(5))
                .deletedAt(null)
                .build();
        regularFood1 = foodRepository.save(regularFood1);

        regularFood2 = Food.builder()
                .foodName("일반메뉴 2 - 불고기")
                .storeId(testStore.getStoreId())
                .categoryId(1L)
                .description("맛있는 불고기")
                .imageUrl("https://example.com/bulgogi.jpg")
                .averagePrice(12000)
                .price(12000)
                .isMain(false)
                .displayOrder(4)
                .registeredDt(LocalDateTime.now().minusDays(3))
                .deletedAt(null)
                .build();
        regularFood2 = foodRepository.save(regularFood2);
    }

    @Test
    @DisplayName("가게별 메뉴 목록 조회 성공 - 기본 정렬 (displayOrder)")
    void getStoreFoods_success_default_sort() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}/foods", testStore.getStoreId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeId").value(testStore.getStoreId()))
                .andExpect(jsonPath("$.data.storeName").value("테스트 식당"))
                .andExpect(jsonPath("$.data.foods").isArray())
                .andExpect(jsonPath("$.data.foods.length()").value(4))
                .andDo(document("get-store-foods-default",
                        pathParameters(
                                parameterWithName("storeId").description("가게 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING).description("가게명"),
                                fieldWithPath("data.foods").type(JsonFieldType.ARRAY).description("메뉴 목록"),
                                fieldWithPath("data.foods[].foodId").type(JsonFieldType.NUMBER).description("메뉴 ID"),
                                fieldWithPath("data.foods[].foodName").type(JsonFieldType.STRING).description("메뉴명"),
                                fieldWithPath("data.foods[].price").type(JsonFieldType.NUMBER).description("가격"),
                                fieldWithPath("data.foods[].description").type(JsonFieldType.STRING).description("설명"),
                                fieldWithPath("data.foods[].imageUrl").type(JsonFieldType.STRING).description("메뉴 이미지 URL"),
                                fieldWithPath("data.foods[].isMain").type(JsonFieldType.BOOLEAN).description("대표 메뉴 여부"),
                                fieldWithPath("data.foods[].displayOrder").type(JsonFieldType.NUMBER).description("표시 순서"),
                                fieldWithPath("data.foods[].isAvailable").type(JsonFieldType.BOOLEAN).description("판매 가능 여부"),
                                fieldWithPath("data.foods[].registeredDt").type(JsonFieldType.STRING).description("등록 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("가게별 메뉴 목록 조회 성공 - 가격 오름차순 정렬")
    void getStoreFoods_success_sort_by_price_asc() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}/foods", testStore.getStoreId())
                        .param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foods[0].price").value(7500))  // 된장찌개
                .andExpect(jsonPath("$.data.foods[1].price").value(8000))  // 김치찌개
                .andExpect(jsonPath("$.data.foods[2].price").value(9000))  // 비빔밥
                .andExpect(jsonPath("$.data.foods[3].price").value(12000)) // 불고기
                .andDo(document("get-store-foods-sort-price-asc",
                        pathParameters(
                                parameterWithName("storeId").description("가게 ID")
                        ),
                        queryParameters(
                                parameterWithName("sort").description("정렬 기준 (price,asc)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING).description("가게명"),
                                fieldWithPath("data.foods").type(JsonFieldType.ARRAY).description("메뉴 목록 (가격 오름차순)"),
                                fieldWithPath("data.foods[].foodId").type(JsonFieldType.NUMBER).description("메뉴 ID"),
                                fieldWithPath("data.foods[].foodName").type(JsonFieldType.STRING).description("메뉴명"),
                                fieldWithPath("data.foods[].price").type(JsonFieldType.NUMBER).description("가격"),
                                fieldWithPath("data.foods[].description").type(JsonFieldType.STRING).description("설명"),
                                fieldWithPath("data.foods[].imageUrl").type(JsonFieldType.STRING).description("메뉴 이미지 URL"),
                                fieldWithPath("data.foods[].isMain").type(JsonFieldType.BOOLEAN).description("대표 메뉴 여부"),
                                fieldWithPath("data.foods[].displayOrder").type(JsonFieldType.NUMBER).description("표시 순서"),
                                fieldWithPath("data.foods[].isAvailable").type(JsonFieldType.BOOLEAN).description("판매 가능 여부"),
                                fieldWithPath("data.foods[].registeredDt").type(JsonFieldType.STRING).description("등록 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보")
                        )
                ));
    }

    @Test
    @DisplayName("가게별 메뉴 목록 조회 성공 - 대표 메뉴 우선 정렬")
    void getStoreFoods_success_sort_by_isMain() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}/foods", testStore.getStoreId())
                        .param("sort", "isMain,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foods[0].isMain").value(true))  // 대표메뉴 먼저
                .andExpect(jsonPath("$.data.foods[1].isMain").value(true))
                .andExpect(jsonPath("$.data.foods[2].isMain").value(false))
                .andExpect(jsonPath("$.data.foods[3].isMain").value(false))
                .andDo(document("get-store-foods-sort-isMain",
                        pathParameters(
                                parameterWithName("storeId").description("가게 ID")
                        ),
                        queryParameters(
                                parameterWithName("sort").description("정렬 기준 (isMain,desc)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING).description("가게명"),
                                fieldWithPath("data.foods").type(JsonFieldType.ARRAY).description("메뉴 목록 (대표 메뉴 우선)"),
                                fieldWithPath("data.foods[].foodId").type(JsonFieldType.NUMBER).description("메뉴 ID"),
                                fieldWithPath("data.foods[].foodName").type(JsonFieldType.STRING).description("메뉴명"),
                                fieldWithPath("data.foods[].price").type(JsonFieldType.NUMBER).description("가격"),
                                fieldWithPath("data.foods[].description").type(JsonFieldType.STRING).description("설명"),
                                fieldWithPath("data.foods[].imageUrl").type(JsonFieldType.STRING).description("메뉴 이미지 URL"),
                                fieldWithPath("data.foods[].isMain").type(JsonFieldType.BOOLEAN).description("대표 메뉴 여부"),
                                fieldWithPath("data.foods[].displayOrder").type(JsonFieldType.NUMBER).description("표시 순서"),
                                fieldWithPath("data.foods[].isAvailable").type(JsonFieldType.BOOLEAN).description("판매 가능 여부"),
                                fieldWithPath("data.foods[].registeredDt").type(JsonFieldType.STRING).description("등록 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보")
                        )
                ));
    }

    @Test
    @DisplayName("가게별 메뉴 목록 조회 성공 - 신메뉴 순 정렬")
    void getStoreFoods_success_sort_by_registeredDt() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}/foods", testStore.getStoreId())
                        .param("sort", "registeredDt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foods[0].foodName").value("일반메뉴 2 - 불고기"))  // 가장 최근
                .andDo(document("get-store-foods-sort-registeredDt",
                        pathParameters(
                                parameterWithName("storeId").description("가게 ID")
                        ),
                        queryParameters(
                                parameterWithName("sort").description("정렬 기준 (registeredDt,desc - 신메뉴 순)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING).description("가게명"),
                                fieldWithPath("data.foods").type(JsonFieldType.ARRAY).description("메뉴 목록 (신메뉴 순)"),
                                fieldWithPath("data.foods[].foodId").type(JsonFieldType.NUMBER).description("메뉴 ID"),
                                fieldWithPath("data.foods[].foodName").type(JsonFieldType.STRING).description("메뉴명"),
                                fieldWithPath("data.foods[].price").type(JsonFieldType.NUMBER).description("가격"),
                                fieldWithPath("data.foods[].description").type(JsonFieldType.STRING).description("설명"),
                                fieldWithPath("data.foods[].imageUrl").type(JsonFieldType.STRING).description("메뉴 이미지 URL"),
                                fieldWithPath("data.foods[].isMain").type(JsonFieldType.BOOLEAN).description("대표 메뉴 여부"),
                                fieldWithPath("data.foods[].displayOrder").type(JsonFieldType.NUMBER).description("표시 순서"),
                                fieldWithPath("data.foods[].isAvailable").type(JsonFieldType.BOOLEAN).description("판매 가능 여부"),
                                fieldWithPath("data.foods[].registeredDt").type(JsonFieldType.STRING).description("등록 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보")
                        )
                ));
    }

    @Test
    @DisplayName("가게별 메뉴 목록 조회 실패 - 존재하지 않는 가게")
    void getStoreFoods_fail_store_not_found() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}/foods", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("get-store-foods-not-found",
                        pathParameters(
                                parameterWithName("storeId").description("존재하지 않는 가게 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드")
                        )
                ));
    }
}
