package com.stdev.smartmealtable.api.food.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
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

import java.math.BigDecimal;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 메뉴 상세 조회 API 통합 테스트
 * TDD: RED-GREEN-REFACTOR
 */
@DisplayName("메뉴 상세 조회 API 테스트")
class GetFoodDetailControllerTest extends AbstractRestDocsTest {
    
    @Autowired
    private FoodRepository foodRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;
    
    @Autowired
    private AddressHistoryRepository addressHistoryRepository;
    
    private Member testMember;
    private Store testStore;
    private Food testFood;
    private String jwtToken;
    
    @BeforeEach
    void setUp() {
        // 테스트 회원 생성 및 저장
        testMember = Member.create(null, "테스트유저", null, RecommendationType.BALANCED);
        testMember = memberRepository.save(testMember);
        
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                testMember.getMemberId(),
                "test@example.com",
                "hashedPassword123",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);
        
        // 기본 주소 생성
        Address addressValue = Address.of(
                "집",
                "서울특별시 강남구 역삼동 825",
                "서울특별시 강남구 강남대로 396",
                "101동 101호",
                37.497952,
                127.027619,
                "HOME"
        );
        AddressHistory testAddress = AddressHistory.create(
                testMember.getMemberId(),
                addressValue,
                true
        );
        addressHistoryRepository.save(testAddress);
        
        // JWT 토큰 생성
        jwtToken = createAccessToken(testMember.getMemberId());
        
        // 테스트 가게 데이터 생성
        testStore = Store.builder()
                .name("교촌치킨 강남점")
                .categoryId(5L)
                .sellerId(1L)
                .address("서울특별시 강남구 테헤란로 123")
                .lotNumberAddress("서울특별시 강남구 역삼동 456-78")
                .latitude(new BigDecimal("37.498123"))
                .longitude(new BigDecimal("127.028456"))
                .phoneNumber("02-1234-5678")
                .description("1991년부터 이어온 전통의 맛")
                .averagePrice(18000)
                .reviewCount(1523)
                .viewCount(8945)
                .favoriteCount(234)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://cdn.smartmealtable.com/stores/101/main.jpg")
                .registeredAt(java.time.LocalDateTime.now())
                .build();
        testStore = storeRepository.save(testStore);
        
        // 테스트 음식 데이터 생성
        testFood = Food.create(
                "교촌 오리지널",
                testStore.getStoreId(),
                5L,
                "교촌의 시그니처 메뉴",
                "https://cdn.smartmealtable.com/foods/201.jpg",
                18000
        );
        testFood = foodRepository.save(testFood);
    }
    
    @Test
    @DisplayName("메뉴 상세 조회 성공")
    void testGetFoodDetail_Success() throws Exception {
        // Given - 모든 데이터는 setUp()에서 준비됨
        
        // When & Then
        mockMvc.perform(get("/api/v1/foods/{foodId}", testFood.getFoodId())
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foodId").value(testFood.getFoodId()))
                .andExpect(jsonPath("$.data.foodName").value("교촌 오리지널"))
                .andExpect(jsonPath("$.data.price").value(18000))
                .andExpect(jsonPath("$.data.description").value("교촌의 시그니처 메뉴"))
                .andExpect(jsonPath("$.data.store.storeId").value(testStore.getStoreId()))
                .andExpect(jsonPath("$.data.store.storeName").value("교촌치킨 강남점"))
                .andExpect(jsonPath("$.data.isAvailable").value(true))
                .andExpect(jsonPath("$.error").isEmpty())
                .andDo(document("food/get-food-detail",
                        pathParameters(
                                parameterWithName("foodId").description("조회할 메뉴의 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data.foodId").description("메뉴 고유 식별자"),
                                fieldWithPath("data.foodName").description("메뉴 이름"),
                                fieldWithPath("data.description").description("메뉴 설명"),
                                fieldWithPath("data.price").description("메뉴 가격"),
                                fieldWithPath("data.imageUrl").description("메뉴 이미지 URL"),
                                fieldWithPath("data.store.storeId").description("가게 고유 식별자"),
                                fieldWithPath("data.store.storeName").description("가게 이름"),
                                fieldWithPath("data.store.categoryName").description("가게 카테고리"),
                                fieldWithPath("data.store.address").description("가게 주소"),
                                fieldWithPath("data.store.phoneNumber").description("가게 전화번호"),
                                fieldWithPath("data.store.averagePrice").description("가게 평균 가격"),
                                fieldWithPath("data.store.reviewCount").description("가게 리뷰 수"),
                                fieldWithPath("data.store.imageUrl").description("가게 이미지 URL"),
                                fieldWithPath("data.isAvailable").description("메뉴 판매 가능 여부"),
                                fieldWithPath("data.budgetComparison.userMealBudget").optional().description("사용자 끼니별 예산"),
                                fieldWithPath("data.budgetComparison.foodPrice").optional().description("메뉴 가격"),
                                fieldWithPath("data.budgetComparison.difference").optional().description("예산과의 차이"),
                                fieldWithPath("data.budgetComparison.isOverBudget").optional().description("예산 초과 여부"),
                                fieldWithPath("data.budgetComparison.differenceText").optional().description("예산 차이 텍스트"),
                                fieldWithPath("error").description("에러 정보 (null인 경우 성공)")
                        )
                ));
    }
    
    @Test
    @DisplayName("메뉴 상세 조회 - 존재하지 않는 메뉴")
    void testGetFoodDetail_NotFound() throws Exception {
        // Given - 존재하지 않는 메뉴 ID
        Long invalidFoodId = 99999L;
        
        // When & Then
        mockMvc.perform(get("/api/v1/foods/{foodId}", invalidFoodId)
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 음식입니다."));
    }
    
    @Test
    @DisplayName("메뉴 상세 조회 - 인증 정보 없음")
    void testGetFoodDetail_Unauthorized() throws Exception {
        // Given - JWT 토큰 없음
        
        // When & Then
        mockMvc.perform(get("/api/v1/foods/{foodId}", testFood.getFoodId())
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
