package com.stdev.smartmealtable.api.expenditure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.config.MockChatModelConfig;
import com.stdev.smartmealtable.api.expenditure.dto.request.CreateExpenditureRequest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 지출 내역 등록 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(MockChatModelConfig.class)
class CreateExpenditureControllerTest extends AbstractContainerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private FoodRepository foodRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private String accessToken;
    private Long memberId;
    private Long categoryId;
    private Long food1Id;  // 싸이버거 세트
    private Long food2Id;  // 치킨버거 세트
    
    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        Member member = Member.create(null, "테스트사용자", null, RecommendationType.BALANCED);
        Member savedMember = memberRepository.save(member);
        memberId = savedMember.getMemberId();
        
        // Access Token 생성
        accessToken = jwtTokenProvider.createToken(memberId);
        
        // 테스트용 카테고리 생성 (reconstitute 패턴 사용)
        Category category = Category.reconstitute(null, "한식");
        Category savedCategory = categoryRepository.save(category);
        categoryId = savedCategory.getCategoryId();
        
        // 테스트용 가게 생성
        Store testStore = Store.builder()
                .name("테스트 음식점")
                .categoryId(categoryId)
                .sellerId(1L)
                .address("서울특별시 강남구 테헤란로 100")
                .lotNumberAddress("서울특별시 강남구 역삼동 100-10")
                .latitude(new BigDecimal("37.5015678"))
                .longitude(new BigDecimal("127.0395432"))
                .phoneNumber("02-1234-5678")
                .description("테스트용 가게")
                .averagePrice(8000)
                .reviewCount(100)
                .viewCount(500)
                .favoriteCount(20)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store.jpg")
                .registeredAt(LocalDateTime.now().minusMonths(1))
                .build();
        Store savedStore = storeRepository.save(testStore);
        
        // 테스트용 음식 생성
        Food food1 = Food.reconstitute(null, "싸이버거 세트", savedStore.getStoreId(), categoryId, "매운 버거", "images/food1.jpg", 6500);
        Food savedFood1 = foodRepository.save(food1);
        food1Id = savedFood1.getFoodId();
        
        Food food2 = Food.reconstitute(null, "치킨버거 세트", savedStore.getStoreId(), categoryId, "바삭한 치킨 버거", "images/food2.jpg", 7000);
        Food savedFood2 = foodRepository.save(food2);
        food2Id = savedFood2.getFoodId();
    }
    
    @Test
    @DisplayName("지출 내역 등록 성공 - 201 Created")
    void createExpenditure_Success() throws Exception {
        // Given
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "맘스터치 강남점",
                13500,
                LocalDate.of(2025, 10, 8),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                "동료와 점심",
                List.of(
                        new CreateExpenditureRequest.ExpenditureItemRequest(food1Id, 1, 6500),
                        new CreateExpenditureRequest.ExpenditureItemRequest(food2Id, 1, 7000)
                )
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.expenditureId").exists())
                .andExpect(jsonPath("$.data.storeName").value("맘스터치 강남점"))
                .andExpect(jsonPath("$.data.amount").value(13500))
                .andExpect(jsonPath("$.data.expendedDate").value("2025-10-08"))
                .andExpect(jsonPath("$.data.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.data.memo").value("동료와 점심"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }
    
    @Test
    @DisplayName("지출 내역 등록 실패 - 항목 총액과 지출 금액 불일치 (400)")
    void createExpenditure_Fail_ItemsTotalMismatch() throws Exception {
        // Given
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "맘스터치 강남점",
                15000,  // 항목 총액(13500)과 불일치
                LocalDate.of(2025, 10, 8),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                "동료와 점심",
                List.of(
                        new CreateExpenditureRequest.ExpenditureItemRequest(food1Id, 1, 6500),
                        new CreateExpenditureRequest.ExpenditureItemRequest(food2Id, 1, 7000)
                )
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())  // 422 -> 400으로 변경 (도메인 검증 예외는 IllegalArgumentException -> 400)
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E400"))  // E422 -> E400으로 변경
                .andExpect(jsonPath("$.error.message").exists());
    }
    
    @Test
    @DisplayName("지출 내역 등록 실패 - 존재하지 않는 카테고리 (404)")
    void createExpenditure_Fail_CategoryNotFound() throws Exception {
        // Given
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "맘스터치 강남점",
                13500,
                LocalDate.of(2025, 10, 8),
                LocalTime.of(12, 30),
                99999L,  // 존재하지 않는 카테고리 ID
                MealType.LUNCH,
                "동료와 점심",
                List.of(
                        new CreateExpenditureRequest.ExpenditureItemRequest(food1Id, 1, 6500),
                        new CreateExpenditureRequest.ExpenditureItemRequest(food2Id, 1, 7000)
                )
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("카테고리를 찾을 수 없습니다. ID: 99999"));
    }
    
    @Test
    @DisplayName("지출 내역 등록 실패 - 인증 토큰 없음 (401)")
    void createExpenditure_Fail_NoToken() throws Exception {
        // Given
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "맘스터치 강남점",
                13500,
                LocalDate.of(2025, 10, 8),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                "동료와 점심",
                List.of(
                        new CreateExpenditureRequest.ExpenditureItemRequest(food1Id, 1, 6500),
                        new CreateExpenditureRequest.ExpenditureItemRequest(food2Id, 1, 7000)
                )
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/expenditures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"));
    }
    
    @Test
    @DisplayName("지출 내역 등록 실패 - 유효하지 않은 요청 (가게 이름 누락, 422)")
    void createExpenditure_Fail_InvalidRequest_NoStoreName() throws Exception {
        // Given
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "",  // 빈 문자열
                13500,
                LocalDate.of(2025, 10, 8),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                "동료와 점심",
                List.of(
                        new CreateExpenditureRequest.ExpenditureItemRequest(food1Id, 1, 6500),
                        new CreateExpenditureRequest.ExpenditureItemRequest(food2Id, 1, 7000)
                )
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }
    
    @Test
    @DisplayName("지출 내역 등록 성공 - 항목 없이 등록")
    void createExpenditure_Success_NoItems() throws Exception {
        // Given
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "스타벅스 강남점",
                4500,
                LocalDate.of(2025, 10, 8),
                LocalTime.of(14, 00),
                categoryId,
                MealType.OTHER,
                "커피 한 잔",
                null  // 항목 없음
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.expenditureId").exists())
                .andExpect(jsonPath("$.data.amount").value(4500));
    }
}
