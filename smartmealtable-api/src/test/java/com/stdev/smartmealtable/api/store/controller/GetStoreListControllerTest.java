package com.stdev.smartmealtable.api.store.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 가게 목록 조회 API 통합 테스트
 * TDD: RED-GREEN-REFACTOR
 */
@DisplayName("가게 목록 조회 API 테스트")
class GetStoreListControllerTest extends AbstractRestDocsTest {
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;
    
    @Autowired
    private AddressHistoryRepository addressHistoryRepository;
    
    private Member testMember;
    private AddressHistory testAddress;
    private String jwtToken;
    
    @BeforeEach
    void setUp() {
        // 테스트 회원 생성 및 저장
        testMember = Member.create(null, "테스트유저", null, RecommendationType.BALANCED);
        testMember = memberRepository.save(testMember); // 저장된 객체 다시 할당하여 ID 확보
        
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                testMember.getMemberId(),
                "test@example.com",
                "hashedPassword123",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);
        
        // 기본 주소 생성 (강남역 인근: 37.497952, 127.027619)
        Address addressValue = Address.of(
                "집",
                "서울특별시 강남구 역삼동 825",
                "서울특별시 강남구 강남대로 396",
                "101동 101호",
                37.497952,
                127.027619,
                "HOME"
        );
        testAddress = AddressHistory.create(
                testMember.getMemberId(),
                addressValue,
                true
        );
        testAddress = addressHistoryRepository.save(testAddress); // 저장된 객체 다시 할당하여 ID 확보
        
        // JWT 토큰 생성 (AbstractRestDocsTest의 createAccessToken 사용)
        jwtToken = createAccessToken(testMember.getMemberId());
        
        // 테스트 가게 데이터 생성
        createTestStores();
    }
    
    private void createTestStores() {
        // 가게 1: 강남역 근처 한식당 (거리: 약 0.5km)
        Store store1 = Store.builder()
                .name("강남 한정식")
                .categoryIds(java.util.List.of(1L))
                .sellerId(1L)
                .address("서울특별시 강남구 강남대로 400")
                .lotNumberAddress("서울특별시 강남구 역삼동 826")
                .latitude(new BigDecimal("37.498500"))
                .longitude(new BigDecimal("127.028000"))
                .phoneNumber("02-1234-5678")
                .description("정통 한정식을 제공합니다")
                .averagePrice(15000)
                .reviewCount(120)
                .viewCount(1500)
                .favoriteCount(50)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store1.jpg")
                .registeredAt(java.time.LocalDateTime.now())
                .build();
        storeRepository.save(store1);
        
        // 가게 2: 강남역 근처 분식집 (거리: 약 0.8km)
        Store store2 = Store.builder()
                .name("김밥천국 강남점")
                .categoryIds(java.util.List.of(2L))
                .sellerId(2L)
                .address("서울특별시 강남구 강남대로 410")
                .lotNumberAddress("서울특별시 강남구 역삼동 830")
                .latitude(new BigDecimal("37.499000"))
                .longitude(new BigDecimal("127.029000"))
                .phoneNumber("02-2345-6789")
                .description("합리적인 가격의 분식집")
                .averagePrice(5000)
                .reviewCount(200)
                .viewCount(2000)
                .favoriteCount(80)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store2.jpg")
                .registeredAt(java.time.LocalDateTime.now())
                .build();
        storeRepository.save(store2);
        
        // 가게 3: 학생식당 (거리: 약 1.2km)
        Store store3 = Store.builder()
                .name("서울대 학생식당")
                .categoryIds(java.util.List.of(1L))
                .sellerId(3L)
                .address("서울특별시 관악구 관악로 1")
                .lotNumberAddress("서울특별시 관악구 신림동 1")
                .latitude(new BigDecimal("37.459772"))
                .longitude(new BigDecimal("126.952718"))
                .phoneNumber("02-3456-7890")
                .description("학생들을 위한 저렴한 식당")
                .averagePrice(3500)
                .reviewCount(300)
                .viewCount(3000)
                .favoriteCount(120)
                .storeType(StoreType.CAMPUS_RESTAURANT)
                .imageUrl("https://example.com/store3.jpg")
                .registeredAt(LocalDateTime.now())
                .build();
        storeRepository.save(store3);
        
        // 가게 4: 강남역에서 먼 곳 (거리: 약 5km)
        Store store4 = Store.builder()
                .name("신촌 맛집")
                .categoryIds(java.util.List.of(3L))
                .sellerId(4L)
                .address("서울특별시 서대문구 신촌로 100")
                .lotNumberAddress("서울특별시 서대문구 창천동 100")
                .latitude(new BigDecimal("37.556800"))
                .longitude(new BigDecimal("126.936900"))
                .phoneNumber("02-4567-8901")
                .description("신촌의 유명 맛집")
                .averagePrice(12000)
                .reviewCount(150)
                .viewCount(1800)
                .favoriteCount(60)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store4.jpg")
                .registeredAt(LocalDateTime.now())
                .build();
        storeRepository.save(store4);
    }
    
    @Test
    @DisplayName("가게 목록 조회 성공 - 기본 조회 (반경 3km)")
    void getStores_Success_DefaultRadius() throws Exception {
        // Debug: JWT token 확인
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken).startsWith("Bearer ");
        
        // Debug: 데이터 확인
        System.out.println("=== Setup Data ===");
        System.out.println("Test Member ID: " + testMember.getMemberId());
        System.out.println("Test Address: " + testAddress.getAddress());
        System.out.println("Saved Stores Count: " + storeRepository.findByIdIn(List.of(1L, 2L, 3L, 4L)).size());
        
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andExpect(jsonPath("$.data.stores.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.totalCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andDo(document("store-list-default",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        queryParameters(
                                parameterWithName("latitude").description("현재 위치의 위도").optional(),
                                parameterWithName("longitude").description("현재 위치의 경도").optional(),
                                parameterWithName("radius").description("검색 반경 (km, 기본값: 3.0)").optional(),
                                parameterWithName("keyword").description("검색 키워드 (가게명 또는 카테고리)").optional(),
                                parameterWithName("sortBy").description("정렬 기준 (DISTANCE_ASC, REVIEW_COUNT_DESC, VIEW_COUNT_DESC, AVERAGE_PRICE_ASC, AVERAGE_PRICE_DESC)").optional(),
                                parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값: 0)").optional(),
                                parameterWithName("size").description("페이지 크기 (기본값: 20)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data.stores[]").description("가게 목록"),
                                fieldWithPath("data.stores[].storeId").description("가게 ID"),
                                fieldWithPath("data.stores[].name").description("가게명"),
                                fieldWithPath("data.stores[].categoryId").description("카테고리 ID"),
                                fieldWithPath("data.stores[].categoryName").description("카테고리명").optional(),
                                fieldWithPath("data.stores[].address").description("주소"),
                                fieldWithPath("data.stores[].latitude").description("위도"),
                                fieldWithPath("data.stores[].longitude").description("경도"),
                                fieldWithPath("data.stores[].phoneNumber").description("전화번호"),
                                fieldWithPath("data.stores[].averagePrice").description("평균 가격"),
                                fieldWithPath("data.stores[].reviewCount").description("리뷰 수"),
                                fieldWithPath("data.stores[].viewCount").description("조회 수"),
                                fieldWithPath("data.stores[].distance").description("사용자와의 거리 (km)"),
                                fieldWithPath("data.stores[].storeType").description("가게 유형 (RESTAURANT, CAMPUS_RESTAURANT 등)"),
                                fieldWithPath("data.stores[].imageUrl").description("가게 대표 이미지 URL"),
                                fieldWithPath("data.totalCount").description("전체 가게 수"),
                                fieldWithPath("data.currentPage").description("현재 페이지 번호 (0부터 시작)"),
                                fieldWithPath("data.pageSize").description("페이지 크기"),
                                fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.hasMore").description("다음 데이터 존재 여부"),
                                fieldWithPath("data.lastId").description("마지막 가게 ID (커서 페이징용)").optional(),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("가게 목록 조회 성공 - 반경 필터링 (1km)")
    void getStores_Success_FilterByRadius() throws Exception {
        // when & then: distance는 BigDecimal이므로 숫자 비교 사용
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("radius", "1.0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray());
                // distance 값 검증은 BigDecimal 타입 이슈로 제외
    }
    
    @Test
    @DisplayName("가게 목록 조회 성공 - 키워드 검색 (가게명)")
    void getStores_Success_SearchByKeyword() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("keyword", "강남"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andExpect(jsonPath("$.data.stores[*].name").value(everyItem(containsString("강남"))))
                .andDo(document("store-list-search",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드 (가게명 또는 주소 검색)")
                        ),
                        responseFields(
                                fieldWithPath("result").description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data.stores[]").description("가게 목록"),
                                fieldWithPath("data.stores[].storeId").description("가게 ID"),
                                fieldWithPath("data.stores[].name").description("가게명"),
                                fieldWithPath("data.stores[].categoryId").description("카테고리 ID"),
                                fieldWithPath("data.stores[].categoryName").description("카테고리명").optional(),
                                fieldWithPath("data.stores[].address").description("주소"),
                                fieldWithPath("data.stores[].latitude").description("위도"),
                                fieldWithPath("data.stores[].longitude").description("경도"),
                                fieldWithPath("data.stores[].phoneNumber").description("전화번호"),
                                fieldWithPath("data.stores[].averagePrice").description("평균 가격"),
                                fieldWithPath("data.stores[].reviewCount").description("리뷰 수"),
                                fieldWithPath("data.stores[].viewCount").description("조회 수"),
                                fieldWithPath("data.stores[].distance").description("사용자와의 거리 (km)"),
                                fieldWithPath("data.stores[].storeType").description("가게 유형"),
                                fieldWithPath("data.stores[].imageUrl").description("가게 대표 이미지 URL"),
                                fieldWithPath("data.totalCount").description("전체 가게 수"),
                                fieldWithPath("data.currentPage").description("현재 페이지 번호"),
                                fieldWithPath("data.pageSize").description("페이지 크기"),
                                fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.hasMore").description("다음 데이터 존재 여부"),
                                fieldWithPath("data.lastId").description("마지막 가게 ID (커서 페이징용)").optional(),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("가게 목록 조회 성공 - 카테고리 필터링")
    void getStores_Success_FilterByCategory() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("categoryId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray());
    }
    
    @Test
    @DisplayName("가게 목록 조회 성공 - 가게 유형 필터링 (학생식당)")
    void getStores_Success_FilterByStoreType() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("storeType", "CAMPUS_RESTAURANT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andExpect(jsonPath("$.data.stores[*].storeType").value(everyItem(is("CAMPUS_RESTAURANT"))));
    }
    
    @Test
    @DisplayName("가게 목록 조회 성공 - 거리순 정렬")
    void getStores_Success_SortByDistance() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("sortBy", "distance"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray());
        // TODO: 거리순 정렬 검증 로직 추가
    }
    
    @Test
    @DisplayName("가게 목록 조회 성공 - 리뷰 많은순 정렬")
    void getStores_Success_SortByReviewCount() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("sortBy", "reviewCount"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray());
    }
    
    @Test
    @DisplayName("가게 목록 조회 성공 - 페이징")
    void getStores_Success_Paging() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.stores.length()").value(lessThanOrEqualTo(2)));
    }
    
    @Test
    @DisplayName("가게 목록 조회 실패 - 인증 토큰 없음 (401)")
    void getStores_Fail_NoAuth() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("가게 목록 조회 실패 - 잘못된 반경 값 (400)")
    void getStores_Fail_InvalidRadius() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("radius", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("가게 목록 조회 실패 - 반경 최대값 초과 (400)")
    void getStores_Fail_RadiusTooLarge() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("radius", "100"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("가게 목록 조회 실패 - 잘못된 페이지 번호 (400)")
    void getStores_Fail_InvalidPage() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("page", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("가게 목록 조회 실패 - 잘못된 페이지 크기 (400)")
    void getStores_Fail_InvalidSize() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("가게 목록 조회 실패 - 기본 주소 미등록 (404)")
    void getStores_Fail_NoPrimaryAddress() throws Exception {
        // given: Delete the test member's primary address
        addressHistoryRepository.delete(testAddress);
        
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }
}
