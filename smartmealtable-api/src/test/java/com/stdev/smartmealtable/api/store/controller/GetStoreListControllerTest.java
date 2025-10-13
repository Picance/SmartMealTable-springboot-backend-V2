package com.stdev.smartmealtable.api.store.controller;

import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.config.MockChatModelConfig;
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
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 가게 목록 조회 API 통합 테스트
 * TDD: RED-GREEN-REFACTOR
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
@Import(MockChatModelConfig.class)
class GetStoreListControllerTest extends AbstractContainerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;
    
    @Autowired
    private AddressHistoryRepository addressHistoryRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private Member testMember;
    private AddressHistory testAddress;
    private String jwtToken;
    
    @BeforeEach
    void setUp() {
        // 테스트 회원 생성 및 저장
        testMember = Member.create(null, "테스트유저", RecommendationType.BALANCED);
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
        addressHistoryRepository.save(testAddress);
        
        // JWT 토큰 생성
        jwtToken = "Bearer " + jwtTokenProvider.createToken(testMember.getMemberId());
        
        // 테스트 가게 데이터 생성
        createTestStores();
    }
    
    private void createTestStores() {
        // 가게 1: 강남역 근처 한식당 (거리: 약 0.5km)
        Store store1 = Store.builder()
                .name("강남 한정식")
                .categoryId(1L)
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
                .categoryId(2L)
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
                .categoryId(1L)
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
                .categoryId(3L)
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
                .andExpect(jsonPath("$.data.pageSize").value(20));
    }
    
    @Test
    @DisplayName("가게 목록 조회 성공 - 반경 필터링 (1km)")
    void getStores_Success_FilterByRadius() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .param("radius", "1.0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andExpect(jsonPath("$.data.stores[*].distance").value(everyItem(lessThan(1.0))));
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
                .andExpect(jsonPath("$.data.stores[*].name").value(everyItem(containsString("강남"))));
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
