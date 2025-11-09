package com.stdev.smartmealtable.api.expenditure.controller;

import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.config.MockChatModelConfig;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureItem;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 지출 내역 상세 조회 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(MockChatModelConfig.class)
class GetExpenditureDetailControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private FoodRepository foodRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private ExpenditureRepository expenditureRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private Long memberId;
    private Long anotherMemberId;
    private String accessToken;
    private String anotherAccessToken;
    private Long categoryId;
    private Long foodId1;
    private Long foodId2;
    
    @BeforeEach
    void setUp() {
        // 그룹 생성
        Group group = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시");
        Group savedGroup = groupRepository.save(group);
        
        // 회원 1 생성
        Member member1 = Member.create(savedGroup.getGroupId(), "테스트유저1", null, RecommendationType.BALANCED);
        Member savedMember1 = memberRepository.save(member1);
        memberId = savedMember1.getMemberId();
        accessToken = jwtTokenProvider.createToken(memberId);
        
        // 회원 2 생성 (권한 테스트용)
        Member member2 = Member.create(savedGroup.getGroupId(), "테스트유저2", null, RecommendationType.BALANCED);
        Member savedMember2 = memberRepository.save(member2);
        anotherMemberId = savedMember2.getMemberId();
        anotherAccessToken = jwtTokenProvider.createToken(anotherMemberId);
        
        // 카테고리 생성
        Category category = Category.reconstitute(null, "한식");
        Category savedCategory = categoryRepository.save(category);
        categoryId = savedCategory.getCategoryId();
        
        // 테스트용 가게 생성
        Store testStore = Store.builder()
                .name("테스트 음식점")
                .categoryIds(java.util.List.of(categoryId))
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
        
        // 음식 생성
        Food food1 = Food.reconstitute(null, "김치찌개", savedStore.getStoreId(), categoryId, "얼큰한 김치찌개", "images/food1.jpg", 8000);
        Food savedFood1 = foodRepository.save(food1);
        foodId1 = savedFood1.getFoodId();
        
        Food food2 = Food.reconstitute(null, "불고기", savedStore.getStoreId(), categoryId, "달콤한 불고기", "images/food2.jpg", 9000);
        Food savedFood2 = foodRepository.save(food2);
        foodId2 = savedFood2.getFoodId();
    }

    @Test
    @DisplayName("지출 내역 상세 조회 성공")
    void getExpenditureDetail_Success() throws Exception {
        // given
        ExpenditureItem item1 = ExpenditureItem.create(foodId1, 2, 8000);
        ExpenditureItem item2 = ExpenditureItem.create(foodId2, 1, 9000);
        
        Expenditure expenditure = Expenditure.create(
                memberId,
                "맛있는 식당",
                25000,
                LocalDate.of(2025, 10, 10),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                "회식",
                List.of(item1, item2)
        );
        Expenditure savedExpenditure = expenditureRepository.save(expenditure);
        
        // when & then
        mockMvc.perform(get("/api/v1/expenditures/{id}", savedExpenditure.getExpenditureId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.expenditureId").value(savedExpenditure.getExpenditureId()))
                .andExpect(jsonPath("$.data.storeName").value("맛있는 식당"))
                .andExpect(jsonPath("$.data.amount").value(25000))
                .andExpect(jsonPath("$.data.expendedDate").value("2025-10-10"))
                .andExpect(jsonPath("$.data.categoryName").value("한식"))
                .andExpect(jsonPath("$.data.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.data.memo").value("회식"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2));
    }

    @Test
    @DisplayName("존재하지 않는 지출 내역 조회 시 404 응답")
    void getExpenditureDetail_NotFound() throws Exception {
        // given
        Long nonExistentId = 99999L;
        
        // when & then
        mockMvc.perform(get("/api/v1/expenditures/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    @Test
    @DisplayName("다른 회원의 지출 내역 조회 시 403 응답")
    void getExpenditureDetail_Forbidden() throws Exception {
        // given - 회원1의 지출 내역 생성
        Expenditure expenditure = Expenditure.create(
                memberId,
                "맛있는 식당",
                25000,
                LocalDate.of(2025, 10, 10),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                null,
                List.of()
        );
        Expenditure savedExpenditure = expenditureRepository.save(expenditure);
        
        // when & then - 회원2가 회원1의 지출 내역 조회 시도
        mockMvc.perform(get("/api/v1/expenditures/{id}", savedExpenditure.getExpenditureId())
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E403"));
    }

    @Test
    @DisplayName("삭제된 지출 내역 조회 시 404 응답")
    void getExpenditureDetail_Deleted() throws Exception {
        // given
        Expenditure expenditure = Expenditure.create(
                memberId,
                "맛있는 식당",
                25000,
                LocalDate.of(2025, 10, 10),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                null,
                List.of()
        );
        Expenditure savedExpenditure = expenditureRepository.save(expenditure);
        savedExpenditure.delete();  // 삭제
        expenditureRepository.save(savedExpenditure);
        
        // when & then
        mockMvc.perform(get("/api/v1/expenditures/{id}", savedExpenditure.getExpenditureId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }
}
