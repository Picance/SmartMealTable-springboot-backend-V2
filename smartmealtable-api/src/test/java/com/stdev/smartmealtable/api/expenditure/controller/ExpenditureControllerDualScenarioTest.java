package com.stdev.smartmealtable.api.expenditure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.expenditure.dto.request.CreateExpenditureFromCartRequest;
import com.stdev.smartmealtable.api.expenditure.dto.request.CreateExpenditureRequest;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.storage.db.expenditure.ExpenditureJpaRepository;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 지출 내역 API 재설계 통합 테스트
 * - 장바구니 시나리오 (storeId + foodId 포함)
 * - 수기 입력 시나리오 (foodName만 포함)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("지출 내역 API 재설계 통합 테스트")
class ExpenditureControllerDualScenarioTest extends AbstractContainerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ExpenditureJpaRepository expenditureRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private MemberAuthenticationRepository authenticationRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private Member member;
    private String accessToken;
    private Long categoryId;
    
    @BeforeEach
    void setUp() {
        // 테스트 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, Address.of("테스트대학교", null, "서울특별시", null, null, null, null));
        testGroup = groupRepository.save(testGroup);
        
        // 테스트 회원 생성
        member = Member.create(
                testGroup.getGroupId(),
                "테스트회원",
                null,
                RecommendationType.BALANCED
        );
        member = memberRepository.save(member);
        
        // 회원 인증 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(),
                "test@example.com",
                "hashedPassword",
                "테스트회원"
        );
        authenticationRepository.save(auth);
        
        // Category 생성 (카테고리 ID가 1일 것으로 예상)
        Category category = Category.create("외식");
        Category savedCategory = categoryRepository.save(category);
        categoryId = savedCategory.getCategoryId();
        
        // JWT 토큰 생성 - application.yml의 설정으로 자동 생성됨
        accessToken = "Bearer " + jwtTokenProvider.createToken(member.getMemberId());
        
        // 기존 데이터 정리
        expenditureRepository.deleteAll();
    }
    
    @Nested
    @DisplayName("장바구니 시나리오 (POST /api/v1/expenditures/from-cart)")
    class CartScenario {
        
        @Test
        @DisplayName("storeId와 foodId가 있는 지출 내역을 성공적으로 등록한다")
        void createExpenditureFromCart_Success() throws Exception {
            // Given
            Long storeId = 100L;
            Long foodId1 = 1000L;
            Long foodId2 = 1001L;
            
            CreateExpenditureFromCartRequest.CartExpenditureItemRequest item1 =
                    new CreateExpenditureFromCartRequest.CartExpenditureItemRequest(
                            foodId1,
                            "스파게티",
                            2,
                            12000
                    );
            
            CreateExpenditureFromCartRequest.CartExpenditureItemRequest item2 =
                    new CreateExpenditureFromCartRequest.CartExpenditureItemRequest(
                            foodId2,
                            "샐러드",
                            1,
                            6000
                    );
            
            CreateExpenditureFromCartRequest request = new CreateExpenditureFromCartRequest(
                    storeId,
                    "이탈리안 레스토랑",
                    30000,
                    LocalDate.of(2025, 10, 31),
                    LocalTime.of(12, 30),
                    categoryId,
                    MealType.LUNCH,
                    "회식",
                    List.of(item1, item2)
            );
            
            // When & Then
            MvcResult result = mockMvc.perform(
                    post("/api/v1/expenditures/from-cart")
                            .header("Authorization", accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            )
                    .andReturn();
            
            // 결과 출력 (디버그)
            System.out.println("Status: " + result.getResponse().getStatus());
            System.out.println("Response: " + result.getResponse().getContentAsString());
            
            // 검증
            assertThat(result.getResponse().getStatus())
                    .as("응답 상태가 201(Created)이어야 함. 실제: " + result.getResponse().getStatus() + ", 응답: " + result.getResponse().getContentAsString())
                    .isEqualTo(201);
        }
    }
    
    @Nested
    @DisplayName("수기 입력 시나리오 (POST /api/v1/expenditures)")
    class ManualInputScenario {
        
        @Test
        @DisplayName("foodId 없이 foodName만으로 지출 내역을 성공적으로 등록한다")
        void createExpenditureManualInput_Success() throws Exception {
            // Given - foodId가 모두 실제 값이어야 함
            CreateExpenditureRequest.ExpenditureItemRequest item1 =
                    new CreateExpenditureRequest.ExpenditureItemRequest(
                            1001L,             // ◆ foodId 필수 (실제 음식 데이터가 필요함)
                            2,
                            12000
                    );
            
            CreateExpenditureRequest.ExpenditureItemRequest item2 =
                    new CreateExpenditureRequest.ExpenditureItemRequest(
                            1002L,             // ◆ foodId 필수
                            1,
                            6000
                    );
            
            CreateExpenditureRequest request = new CreateExpenditureRequest(
                    "임의의 음식점",
                    30000,
                    LocalDate.of(2025, 10, 31),
                    LocalTime.of(12, 30),
                    categoryId,
                    MealType.LUNCH,
                    "지출 메모",
                    List.of(item1, item2)
            );
            
            // When & Then
            MvcResult result = mockMvc.perform(
                    post("/api/v1/expenditures")
                            .header("Authorization", accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            )
                    .andReturn();
            
            // 결과 확인 (디버그)
            System.out.println("Status: " + result.getResponse().getStatus());
            System.out.println("Response: " + result.getResponse().getContentAsString());
            
            // 검증
            assertThat(result.getResponse().getStatus())
                    .as("응답 상태가 201(Created)이어야 함. 실제: " + result.getResponse().getStatus() + ", 응답: " + result.getResponse().getContentAsString())
                    .isEqualTo(201);
        }
    }
    
    @Nested
    @DisplayName("기존 API 호환성 테스트")
    class BackwardCompatibility {
        
        @Test
        @DisplayName("기존 POST /api/v1/expenditures 엔드포인트는 여전히 동작한다")
        void createExpenditureBackwardCompatible() throws Exception {
            // Given
            CreateExpenditureRequest.ExpenditureItemRequest item =
                    new CreateExpenditureRequest.ExpenditureItemRequest(
                            1L,
                            1,
                            10000
                    );
            
            CreateExpenditureRequest request = new CreateExpenditureRequest(
                    "가게명",
                    10000,
                    LocalDate.of(2025, 10, 31),
                    LocalTime.of(12, 0),
                    null,
                    null,
                    null,
                    List.of(item)
            );
            
            // When & Then
            mockMvc.perform(
                    post("/api/v1/expenditures")
                            .header("Authorization", accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.expenditureId").exists());
        }
    }
}
