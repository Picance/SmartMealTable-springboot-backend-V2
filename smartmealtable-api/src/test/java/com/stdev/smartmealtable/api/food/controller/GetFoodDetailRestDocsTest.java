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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 메뉴 상세 조회 API REST Docs 테스트
 * 
 * 메뉴 정보와 판매 가게 정보를 함께 조회하는 API 문서화
 */
@DisplayName("메뉴 상세 조회 API REST Docs 테스트")
@Transactional
class GetFoodDetailRestDocsTest extends AbstractRestDocsTest {
    
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
    @DisplayName("메뉴 상세 조회 API - REST Docs")
    void getFoodDetail() throws Exception {
        // Given - 모든 데이터는 setUp()에서 준비됨
        
        // When & Then
        mockMvc.perform(get("/api/v1/foods/{foodId}", testFood.getFoodId())
                .header("Authorization", jwtToken)
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foodId").exists())
                .andExpect(jsonPath("$.data.foodName").exists())
                .andExpect(jsonPath("$.data.store.storeId").exists())
                .andDo(document("food/get-food-detail",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("foodId").description("조회할 메뉴의 ID")
                        ),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("메뉴 상세 정보"),
                                fieldWithPath("data.foodId").type(JsonFieldType.NUMBER)
                                        .description("메뉴 ID"),
                                fieldWithPath("data.foodName").type(JsonFieldType.STRING)
                                        .description("메뉴명"),
                                fieldWithPath("data.description").type(JsonFieldType.STRING)
                                        .description("메뉴 설명"),
                                fieldWithPath("data.price").type(JsonFieldType.NUMBER)
                                        .description("메뉴 가격 (원)"),
                                fieldWithPath("data.imageUrl").type(JsonFieldType.STRING)
                                        .description("메뉴 이미지 URL"),
                                fieldWithPath("data.store").type(JsonFieldType.OBJECT)
                                        .description("판매 가게 정보"),
                                fieldWithPath("data.store.storeId").type(JsonFieldType.NUMBER)
                                        .description("가게 ID"),
                                fieldWithPath("data.store.storeName").type(JsonFieldType.STRING)
                                        .description("가게명"),
                                fieldWithPath("data.store.categoryName").type(JsonFieldType.NULL)
                                        .optional()
                                        .description("가게 카테고리명 (현재는 null)"),
                                fieldWithPath("data.store.address").type(JsonFieldType.STRING)
                                        .description("가게 도로명 주소"),
                                fieldWithPath("data.store.phoneNumber").type(JsonFieldType.STRING)
                                        .description("가게 전화번호"),
                                fieldWithPath("data.store.averagePrice").type(JsonFieldType.NUMBER)
                                        .description("가게 평균 가격 (원)"),
                                fieldWithPath("data.store.reviewCount").type(JsonFieldType.NUMBER)
                                        .description("가게 리뷰 수"),
                                fieldWithPath("data.store.imageUrl").type(JsonFieldType.STRING)
                                        .description("가게 대표 이미지 URL"),
                                fieldWithPath("data.isAvailable").type(JsonFieldType.BOOLEAN)
                                        .description("메뉴 판매 여부"),
                                fieldWithPath("data.budgetComparison").type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("사용자 예산 비교 정보"),
                                fieldWithPath("data.budgetComparison.userMealBudget").type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("사용자 끼니별 예산 (원)"),
                                fieldWithPath("data.budgetComparison.foodPrice").type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("메뉴 가격 (원)"),
                                fieldWithPath("data.budgetComparison.difference").type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("예산과 메뉴 가격의 차이 (원)"),
                                fieldWithPath("data.budgetComparison.isOverBudget").type(JsonFieldType.BOOLEAN)
                                        .optional()
                                        .description("예산 초과 여부"),
                                fieldWithPath("data.budgetComparison.differenceText").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("예산 차이 텍스트 (+1,000원)"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러 정보 (null인 경우 성공)")
                        )
                ));
    }
    
    @Test
    @DisplayName("메뉴 상세 조회 API - 존재하지 않는 메뉴 - REST Docs")
    void getFoodDetail_NotFound() throws Exception {
        // Given - 존재하지 않는 메뉴 ID
        Long invalidFoodId = 99999L;
        
        // When & Then
        mockMvc.perform(get("/api/v1/foods/{foodId}", invalidFoodId)
                .header("Authorization", jwtToken)
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andDo(document("food/get-food-detail-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("foodId").description("조회할 메뉴의 ID (존재하지 않음)")
                        ),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL)
                                        .description("응답 데이터 (에러 발생 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E404)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }
}
