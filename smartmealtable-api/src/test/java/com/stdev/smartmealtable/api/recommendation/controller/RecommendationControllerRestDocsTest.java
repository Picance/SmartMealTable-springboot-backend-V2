package com.stdev.smartmealtable.api.recommendation.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.*;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.math.BigDecimal;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RecommendationController REST Docs 테스트
 */
@DisplayName("RecommendationController REST Docs")
class RecommendationControllerRestDocsTest extends AbstractRestDocsTest {

    /**
     * 공통 에러 응답 필드 정의
     */
    private static FieldDescriptor[] getErrorResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                subsectionWithPath("error.data").type(JsonFieldType.OBJECT).description("에러 상세 정보 (validation 에러 시 필드별 에러)").optional()
        };
    }

    /**
     * 추천 목록 기본 요청 파라미터 정의 (필수만)
     */
    private static org.springframework.restdocs.request.ParameterDescriptor[] getRecommendationBasicRequestParams() {
        return new org.springframework.restdocs.request.ParameterDescriptor[]{
                parameterWithName("latitude").description("현재 위도 (WGS84, 필수)"),
                parameterWithName("longitude").description("현재 경도 (WGS84, 필수)"),
                parameterWithName("radius").description("검색 반경 (km)").optional()
        };
    }

    /**
     * 추천 목록 전체 요청 파라미터 정의 (필수 + 선택)
     */
    private static org.springframework.restdocs.request.ParameterDescriptor[] getRecommendationFullRequestParams() {
        return new org.springframework.restdocs.request.ParameterDescriptor[]{
                parameterWithName("latitude").description("현재 위도 (WGS84, 필수)"),
                parameterWithName("longitude").description("현재 경도 (WGS84, 필수)"),
                parameterWithName("radius").description("검색 반경 (km, 기본값: 0.5)").optional(),
                parameterWithName("sortBy").description("정렬 기준 (SCORE/DISTANCE/REVIEW/PRICE_LOW/PRICE_HIGH/FAVORITE/INTEREST_HIGH/INTEREST_LOW, 기본값: SCORE)").optional(),
                parameterWithName("includeDisliked").description("불호 음식 포함 여부 (기본값: false)").optional(),
                parameterWithName("openNow").description("영업 중인 가게만 조회 (기본값: false)").optional(),
                parameterWithName("storeType").description("가게 타입 필터 (ALL/CAMPUS_RESTAURANT/RESTAURANT, 기본값: ALL)").optional(),
                parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값: 0)").optional(),
                parameterWithName("size").description("페이지 크기 (기본값: 20, 최대: 100)").optional()
        };
    }

    /**
     * 추천 목록 기본 응답 필드 정의
     */
    private static FieldDescriptor[] getRecommendationListResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                fieldWithPath("data").type(JsonFieldType.ARRAY).description("추천 결과 목록"),
                fieldWithPath("data[].storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                fieldWithPath("data[].storeName").type(JsonFieldType.STRING).description("가게 이름"),
                fieldWithPath("data[].categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
                fieldWithPath("data[].score").type(JsonFieldType.NUMBER).description("추천 점수 (0-100)"),
                fieldWithPath("data[].distance").type(JsonFieldType.NUMBER).description("거리 (km)"),
                fieldWithPath("data[].averagePrice").type(JsonFieldType.NUMBER).description("평균 가격"),
                fieldWithPath("data[].reviewCount").type(JsonFieldType.NUMBER).description("리뷰 수"),
                fieldWithPath("data[].imageUrl").type(JsonFieldType.STRING).description("대표 이미지 URL").optional(),
                fieldWithPath("data[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                fieldWithPath("data[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보 (성공 시 null)").optional()
        };
    }

    /**
     * 점수 상세 조회 응답 필드 정의
     */
    private static FieldDescriptor[] getScoreDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                fieldWithPath("data").type(JsonFieldType.OBJECT).description("점수 상세 정보"),
                fieldWithPath("data.storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                fieldWithPath("data.storeName").type(JsonFieldType.STRING).description("가게명"),
                fieldWithPath("data.recommendationType").type(JsonFieldType.STRING).description("사용자 추천 유형 (SAVER/ADVENTURER/BALANCED)"),
                fieldWithPath("data.finalScore").type(JsonFieldType.NUMBER).description("최종 추천 점수 (0-100)"),
                fieldWithPath("data.stabilityScore").type(JsonFieldType.NUMBER).description("안정성 점수 (0-100)"),
                fieldWithPath("data.explorationScore").type(JsonFieldType.NUMBER).description("탐험성 점수 (0-100)"),
                fieldWithPath("data.budgetEfficiencyScore").type(JsonFieldType.NUMBER).description("예산 효율성 점수 (0-100)"),
                fieldWithPath("data.accessibilityScore").type(JsonFieldType.NUMBER).description("접근성 점수 (0-100)"),
                fieldWithPath("data.weightedStabilityScore").type(JsonFieldType.NUMBER).description("가중 안정성 점수"),
                fieldWithPath("data.weightedExplorationScore").type(JsonFieldType.NUMBER).description("가중 탐험성 점수"),
                fieldWithPath("data.weightedBudgetEfficiencyScore").type(JsonFieldType.NUMBER).description("가중 예산 효율성 점수"),
                fieldWithPath("data.weightedAccessibilityScore").type(JsonFieldType.NUMBER).description("가중 접근성 점수"),
                fieldWithPath("data.distance").type(JsonFieldType.NUMBER).description("거리 (km)"),
                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보 (정상 시 null)").optional()
        };
    }

    /**
     * 추천 유형 변경 응답 필드 정의
     */
    private static FieldDescriptor[] getUpdateRecommendationTypeResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                fieldWithPath("data").type(JsonFieldType.OBJECT).description("변경 결과"),
                fieldWithPath("data.memberId").type(JsonFieldType.NUMBER).description("회원 ID"),
                fieldWithPath("data.recommendationType").type(JsonFieldType.STRING).description("변경된 추천 유형"),
                fieldWithPath("data.message").type(JsonFieldType.STRING).description("변경 완료 메시지"),
                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보 (정상 시 null)").optional()
        };
    }

    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberAuthenticationRepository memberAuthenticationRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private AddressHistoryRepository addressHistoryRepository;

    private Member member;
    private String accessToken;
    private Category koreanCategory;
    private Category japaneseCategory;
    private Category chineseCategory;
    private Store store1;
    private Store store2;
    private Store store3;

    @BeforeEach
    void setUpTestData() {
        // 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시 관악구");
        Group savedGroup = groupRepository.save(testGroup);

        // 회원 생성
        Member testMember = Member.create(savedGroup.getGroupId(), "추천테스트유저", RecommendationType.BALANCED);
        member = memberRepository.save(testMember);

        // 회원 인증 정보 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(),
                "recommendation@example.com",
                "hashedPasswordForTest",
                "추천테스트유저"
        );
        memberAuthenticationRepository.save(auth);

        // 기본 주소 등록 (가게 위치와 동일하게 설정)
        Address testAddress = Address.of(
                "집",                                    // alias
                "서울특별시 관악구 봉천동 123-45",         // lotNumberAddress
                "서울특별시 관악구 봉천동 123",            // streetNameAddress
                null,                                   // detailedAddress
                37.4783,                                // latitude
                126.9516,                               // longitude
                "HOME"                                  // addressType
        );
        AddressHistory addressHistory = AddressHistory.create(
                member.getMemberId(),
                testAddress,
                true  // 기본 주소로 설정
        );
        addressHistoryRepository.save(addressHistory);

        // JWT 토큰 생성
        accessToken = createAccessToken(member.getMemberId());

        // 카테고리 생성
        koreanCategory = Category.reconstitute(null, "한식");
        koreanCategory = categoryRepository.save(koreanCategory);

        japaneseCategory = Category.reconstitute(null, "일식");
        japaneseCategory = categoryRepository.save(japaneseCategory);

        chineseCategory = Category.reconstitute(null, "중식");
        chineseCategory = categoryRepository.save(chineseCategory);

        // 가게 생성 (테스트용 Mock 데이터 - 추천 로직은 ApplicationService에서 처리)
        store1 = Store.builder()
                .name("맛있는 한식당")
                .categoryId(koreanCategory.getCategoryId())
                .address("서울특별시 관악구 봉천동 123")
                .lotNumberAddress("서울특별시 관악구 봉천동 123-45")
                .latitude(new BigDecimal("37.4783"))
                .longitude(new BigDecimal("126.9516"))
                .phoneNumber("02-1234-5678")
                .averagePrice(8000)
                .reviewCount(150)
                .viewCount(1200)
                .favoriteCount(50)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store1.jpg")
                .registeredAt(java.time.LocalDateTime.now().minusMonths(6))
                .build();
        store1 = storeRepository.save(store1);

        store2 = Store.builder()
                .name("신선한 일식당")
                .categoryId(japaneseCategory.getCategoryId())
                .address("서울특별시 관악구 신림동 456")
                .lotNumberAddress("서울특별시 관악구 신림동 456-78")
                .latitude(new BigDecimal("37.4800"))
                .longitude(new BigDecimal("126.9300"))
                .phoneNumber("02-8765-4321")
                .averagePrice(15000)
                .reviewCount(200)
                .viewCount(1500)
                .favoriteCount(80)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store2.jpg")
                .registeredAt(java.time.LocalDateTime.now().minusDays(30))
                .build();
        store2 = storeRepository.save(store2);

        store3 = Store.builder()
                .name("중화요리 맛집")
                .categoryId(chineseCategory.getCategoryId())
                .address("서울특별시 관악구 남현동 789")
                .lotNumberAddress("서울특별시 관악구 남현동 789-12")
                .latitude(new BigDecimal("37.4700"))
                .longitude(new BigDecimal("126.9400"))
                .phoneNumber("02-9999-8888")
                .averagePrice(12000)
                .reviewCount(100)
                .viewCount(800)
                .favoriteCount(30)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store3.jpg")
                .registeredAt(java.time.LocalDateTime.now().minusMonths(12))
                .build();
        store3 = storeRepository.save(store3);
    }

    @Test
    @DisplayName("추천 목록 조회 성공 - 기본 조건 - 200")
    void getRecommendations_Success_Default() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .header("Authorization", accessToken)
                        .param("latitude", "37.4783")
                        .param("longitude", "126.9516")
                        .param("radius", "5.0"))  // 반경 5km로 설정하여 모든 테스트 가게 포함
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(document("recommendation-list-success-default",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        queryParameters(getRecommendationBasicRequestParams()),
                        responseFields(getRecommendationListResponseFields())
                ));
    }

    @Test
    @DisplayName("추천 목록 조회 성공 - 필터 및 정렬 옵션 - 200")
    void getRecommendations_Success_WithFilters() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .header("Authorization", accessToken)
                        .param("latitude", "37.4783")
                        .param("longitude", "126.9516")
                        .param("radius", "5.0")
                        .param("sortBy", "DISTANCE")
                        .param("includeDisliked", "false")
                        .param("openNow", "false")
                        .param("storeType", "ALL")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(document("recommendation-list-success-with-filters",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        queryParameters(getRecommendationFullRequestParams()),
                        responseFields(getRecommendationListResponseFields())
                ));
    }

    @Test
    @DisplayName("추천 목록 조회 실패 - 필수 파라미터 누락 (위도) - 400")
    void getRecommendations_Fail_MissingLatitude() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .header("Authorization", accessToken)
                        .param("longitude", "126.9516"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("recommendation-list-fail-missing-latitude",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        queryParameters(
                                parameterWithName("longitude").description("현재 경도 (WGS84)")
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }

    @Test
    @DisplayName("추천 목록 조회 실패 - 필수 파라미터 누락 (경도) - 400")
    void getRecommendations_Fail_MissingLongitude() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .header("Authorization", accessToken)
                        .param("latitude", "37.4783"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("recommendation-list-fail-missing-longitude",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        queryParameters(
                                parameterWithName("latitude").description("현재 위도 (WGS84)")
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }

    @Test
    @DisplayName("추천 목록 조회 실패 - 유효하지 않은 위도 범위 - 400")
    void getRecommendations_Fail_InvalidLatitude() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .header("Authorization", accessToken)
                        .param("latitude", "100.0")
                        .param("longitude", "126.9516"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.message").value("위도는 -90 ~ 90 범위여야 합니다"))
                .andDo(document("recommendation-list-fail-invalid-latitude",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        queryParameters(
                                parameterWithName("latitude").description("유효하지 않은 위도 (100.0)"),
                                parameterWithName("longitude").description("현재 경도 (WGS84)")
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }

    @Test
    @DisplayName("추천 목록 조회 실패 - 유효하지 않은 경도 범위 - 400")
    void getRecommendations_Fail_InvalidLongitude() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .header("Authorization", accessToken)
                        .param("latitude", "37.4783")
                        .param("longitude", "200.0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.message").value("경도는 -180 ~ 180 범위여야 합니다"))
                .andDo(document("recommendation-list-fail-invalid-longitude",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        queryParameters(
                                parameterWithName("latitude").description("현재 위도 (WGS84)"),
                                parameterWithName("longitude").description("유효하지 않은 경도 (200.0)")
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }

    @Test
    @DisplayName("추천 목록 조회 실패 - 유효하지 않은 정렬 기준 - 500")
    void getRecommendations_Fail_InvalidSortBy() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .header("Authorization", accessToken)
                        .param("latitude", "37.4783")
                        .param("longitude", "126.9516")
                        .param("sortBy", "INVALID_SORT"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("recommendation-list-fail-invalid-sortby",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        queryParameters(
                                parameterWithName("latitude").description("현재 위도 (WGS84)"),
                                parameterWithName("longitude").description("현재 경도 (WGS84)"),
                                parameterWithName("sortBy").description("유효하지 않은 정렬 기준 (INVALID_SORT)")
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }

    @Test
    @DisplayName("추천 목록 조회 실패 - 인증 토큰 없음 - 401")
    void getRecommendations_Fail_NoAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .param("latitude", "37.4783")
                        .param("longitude", "126.9516"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("recommendation-list-fail-no-authentication",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("latitude").description("현재 위도 (WGS84)"),
                                parameterWithName("longitude").description("현재 경도 (WGS84)")
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }

    @Test
    @DisplayName("점수 상세 조회 성공 - 200")
    void getScoreDetail_Success() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations/{storeId}/score-detail", store1.getStoreId())
                        .header("Authorization", accessToken)
                        .param("latitude", "37.4783")
                        .param("longitude", "126.9516"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.storeId").value(store1.getStoreId()))
                .andExpect(jsonPath("$.data.storeName").value(store1.getName()))
                .andExpect(jsonPath("$.data.recommendationType").exists())
                .andExpect(jsonPath("$.data.finalScore").exists())
                .andExpect(jsonPath("$.data.stabilityScore").exists())
                .andExpect(jsonPath("$.data.explorationScore").exists())
                .andExpect(jsonPath("$.data.budgetEfficiencyScore").exists())
                .andExpect(jsonPath("$.data.accessibilityScore").exists())
                .andExpect(jsonPath("$.data.distance").exists())
                .andDo(document("recommendation-score-detail-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("storeId").description("점수를 조회할 가게 ID")
                        ),
                        queryParameters(
                                parameterWithName("latitude").description("현재 위도 (WGS84) - 선택").optional(),
                                parameterWithName("longitude").description("현재 경도 (WGS84) - 선택").optional()
                        ),
                        responseFields(getScoreDetailResponseFields())
                ));
    }

    @Test
    @DisplayName("점수 상세 조회 실패 - 가게를 찾을 수 없음 - 400")
    void getScoreDetail_Fail_StoreNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations/{storeId}/score-detail", 999999L)
                        .header("Authorization", accessToken)
                        .param("latitude", "37.4783")
                        .param("longitude", "126.9516"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("recommendation-score-detail-fail-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("storeId").description("존재하지 않는 가게 ID")
                        ),
                        queryParameters(
                                parameterWithName("latitude").description("현재 위도 (WGS84) - 선택").optional(),
                                parameterWithName("longitude").description("현재 경도 (WGS84) - 선택").optional()
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }

    @Test
    @DisplayName("추천 유형 변경 성공 - 200")
    void updateRecommendationType_Success() throws Exception {
        String requestBody = """
                {
                    "recommendationType": "SAVER"
                }
                """;

        mockMvc.perform(put("/api/v1/recommendations/type")
                        .header("Authorization", accessToken)
                        .contentType("application/json")
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.memberId").value(member.getMemberId()))
                .andExpect(jsonPath("$.data.recommendationType").value("SAVER"))
                .andExpect(jsonPath("$.data.message").exists())
                .andDo(document("recommendation-type-update-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("recommendationType").type(JsonFieldType.STRING).description("변경할 추천 유형 (SAVER/ADVENTURER/BALANCED)")
                        ),
                        responseFields(getUpdateRecommendationTypeResponseFields())
                ));
    }

    @Test
    @DisplayName("추천 유형 변경 실패 - 필수 파라미터 누락 - 422")
    void updateRecommendationType_Fail_MissingParameter() throws Exception {
        String requestBody = """
                {
                }
                """;

        mockMvc.perform(put("/api/v1/recommendations/type")
                        .header("Authorization", accessToken)
                        .contentType("application/json")
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("recommendation-type-update-fail-missing-parameter",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }

    @Test
    @DisplayName("추천 유형 변경 실패 - 유효하지 않은 추천 유형 - 500")
    void updateRecommendationType_Fail_InvalidType() throws Exception {
        String requestBody = """
                {
                    "recommendationType": "INVALID_TYPE"
                }
                """;

        mockMvc.perform(put("/api/v1/recommendations/type")
                        .header("Authorization", accessToken)
                        .contentType("application/json")
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("recommendation-type-update-fail-invalid-type",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("recommendationType").type(JsonFieldType.STRING).description("유효하지 않은 추천 유형")
                        ),
                        responseFields(getErrorResponseFields())
                ));
    }
}
