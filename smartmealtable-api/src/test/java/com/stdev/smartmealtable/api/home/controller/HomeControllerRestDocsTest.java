package com.stdev.smartmealtable.api.home.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.home.controller.request.MonthlyBudgetConfirmRequest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.budget.MealBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.entity.*;
import com.stdev.smartmealtable.domain.member.repository.*;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HomeController REST Docs 테스트
 */
@DisplayName("HomeController REST Docs 테스트")
class HomeControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository authenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AddressHistoryRepository addressHistoryRepository;

    @Autowired
    private MonthlyBudgetRepository monthlyBudgetRepository;

    @Autowired
    private DailyBudgetRepository dailyBudgetRepository;

    @Autowired
    private MealBudgetRepository mealBudgetRepository;

    @Autowired
    private MonthlyBudgetConfirmationRepository monthlyBudgetConfirmationRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member member;
    private String accessToken;
    private Store savedStore;
    private Food savedFood;

    @BeforeEach
    void setUp() {
        // 테스트 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, Address.of("테스트대학교", null, "서울특별시", null, null, null, null));
        Group savedGroup = groupRepository.save(testGroup);

        // 테스트 회원 생성
        member = Member.create(savedGroup.getGroupId(), "홈테스트회원", null, RecommendationType.SAVER);
        member = memberRepository.save(member);

        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(), "home@example.com", "hashedPassword", "홈테스트회원"
        );
        authenticationRepository.save(auth);

        // JWT 토큰 생성
        accessToken = createAccessToken(member.getMemberId());

        // 주소 데이터 생성
        Address addressValue = Address.of(
                "집",
                "대전광역시 유성구 궁동 1234",
                "대전광역시 유성구 궁동로 1",
                "101동 101호",
                36.3504,
                127.3845,
                AddressType.HOME
        );
        AddressHistory addressHistory = AddressHistory.create(
                member.getMemberId(),
                addressValue,
                true
        );
        addressHistoryRepository.save(addressHistory);

        // 예산 데이터 생성
        String currentMonth = String.format("%d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        MonthlyBudget monthlyBudget = MonthlyBudget.create(member.getMemberId(), 500000, currentMonth);
        monthlyBudgetRepository.save(monthlyBudget);

        DailyBudget dailyBudget = DailyBudget.create(member.getMemberId(), 17000, LocalDate.now());
        dailyBudget = dailyBudgetRepository.save(dailyBudget);

        // 끼니별 예산 데이터 생성
        MealBudget breakfast = MealBudget.create(dailyBudget.getBudgetId(), 5000, MealType.BREAKFAST, LocalDate.now());
        MealBudget lunch = MealBudget.create(dailyBudget.getBudgetId(), 7000, MealType.LUNCH, LocalDate.now());
        MealBudget dinner = MealBudget.create(dailyBudget.getBudgetId(), 5000, MealType.DINNER, LocalDate.now());
        
        mealBudgetRepository.save(breakfast);
        mealBudgetRepository.save(lunch);
        mealBudgetRepository.save(dinner);

        // 추천 테스트용 카테고리/가게/메뉴 데이터 생성
        Category category = categoryRepository.save(Category.create("한식"));

        savedStore = storeRepository.save(Store.builder()
                .name("맛있는집")
                .categoryIds(List.of(category.getCategoryId()))
                .address("대전광역시 유성구 궁동로 1")
                .lotNumberAddress("대전광역시 유성구 궁동 1234")
                .latitude(BigDecimal.valueOf(36.351))
                .longitude(BigDecimal.valueOf(127.385))
                .averagePrice(7500)
                .reviewCount(523)
                .viewCount(1000)
                .favoriteCount(120)
                .imageUrl("https://cdn.smartmealtable.com/stores/101/main.jpg")
                .storeType(StoreType.RESTAURANT)
                .registeredAt(java.time.LocalDateTime.now())
                .build());

        savedFood = foodRepository.save(Food.builder()
                .foodName("김치찌개")
                .storeId(savedStore.getStoreId())
                .categoryId(category.getCategoryId())
                .description("얼큰한 김치찌개")
                .imageUrl("https://cdn.smartmealtable.com/foods/201.jpg")
                .averagePrice(7000)
                .price(7000)
                .isMain(true)
                .displayOrder(1)
                .build());
    }

    @Test
    @DisplayName("홈 대시보드 조회 성공")
    void getHomeDashboard_success_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/home/dashboard")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.location").exists())
                .andExpect(jsonPath("$.data.budget").exists())
                .andExpect(jsonPath("$.data.budget.remaining").value(17000))
                .andExpect(jsonPath("$.data.recommendedMenus[0].foodId").value(savedFood.getFoodId().intValue()))
                .andExpect(jsonPath("$.data.recommendedMenus[0].storeId").value(savedStore.getStoreId().intValue()))
                .andExpect(jsonPath("$.data.recommendedMenus[0].price").value(savedFood.getPrice()))
                .andExpect(jsonPath("$.data.recommendedMenus[0].imageUrl").value(savedFood.getImageUrl()))
                .andExpect(jsonPath("$.data.recommendedMenus[0].tag").isNotEmpty())
                .andExpect(jsonPath("$.data.recommendedStores[0].storeId").value(savedStore.getStoreId().intValue()))
                .andExpect(jsonPath("$.data.recommendedStores[0].averagePrice").value(savedStore.getAveragePrice()))
                .andExpect(jsonPath("$.data.recommendedStores[0].reviewCount").value(savedStore.getReviewCount()))
                .andExpect(jsonPath("$.data.recommendedStores[0].imageUrl").value(savedStore.getImageUrl()))
                .andExpect(jsonPath("$.data.recommendedStores[0].tag").isNotEmpty())
                .andDo(document("home/dashboard-get-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("홈 대시보드 데이터"),
                                fieldWithPath("data.location")
                                        .type(JsonFieldType.OBJECT)
                                        .description("위치 정보"),
                                fieldWithPath("data.location.addressHistoryId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("주소 이력 ID"),
                                fieldWithPath("data.location.addressAlias")
                                        .type(JsonFieldType.STRING)
                                        .description("주소 별칭"),
                                fieldWithPath("data.location.fullAddress")
                                        .type(JsonFieldType.STRING)
                                        .description("전체 주소 (지번)"),
                                fieldWithPath("data.location.roadAddress")
                                        .type(JsonFieldType.STRING)
                                        .description("도로명 주소"),
                                fieldWithPath("data.location.latitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("위도"),
                                fieldWithPath("data.location.longitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("경도"),
                                fieldWithPath("data.location.isPrimary")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("주 주소 여부"),
                                fieldWithPath("data.budget")
                                        .type(JsonFieldType.OBJECT)
                                        .description("예산 정보"),
                                fieldWithPath("data.budget.todayBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("오늘의 예산"),
                                fieldWithPath("data.budget.todaySpent")
                                        .type(JsonFieldType.NUMBER)
                                        .description("오늘 사용 금액"),
                                fieldWithPath("data.budget.remaining")
                                        .type(JsonFieldType.NUMBER)
                                        .description("오늘 남은 금액"),
                                fieldWithPath("data.budget.utilizationRate")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예산 사용률 (%)"),
                                fieldWithPath("data.budget.mealBudgets")
                                        .type(JsonFieldType.ARRAY)
                                        .description("끼니별 예산 정보"),
                                fieldWithPath("data.budget.mealBudgets[].mealType")
                                        .type(JsonFieldType.STRING)
                                        .description("식사 타입 (BREAKFAST/LUNCH/DINNER/OTHER)"),
                                fieldWithPath("data.budget.mealBudgets[].budget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("끼니별 예산"),
                                fieldWithPath("data.budget.mealBudgets[].spent")
                                        .type(JsonFieldType.NUMBER)
                                        .description("끼니별 사용 금액"),
                                fieldWithPath("data.budget.mealBudgets[].remaining")
                                        .type(JsonFieldType.NUMBER)
                                        .description("끼니별 남은 금액"),
                                fieldWithPath("data.recommendedMenus")
                                        .type(JsonFieldType.ARRAY)
                                        .description("추천 메뉴 목록"),
                                fieldWithPath("data.recommendedMenus[].foodId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("음식 ID"),
                                fieldWithPath("data.recommendedMenus[].foodName")
                                        .type(JsonFieldType.STRING)
                                        .description("음식 이름"),
                                fieldWithPath("data.recommendedMenus[].price")
                                        .type(JsonFieldType.NUMBER)
                                        .description("메뉴 가격"),
                                fieldWithPath("data.recommendedMenus[].storeId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 ID"),
                                fieldWithPath("data.recommendedMenus[].storeName")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 이름"),
                                fieldWithPath("data.recommendedMenus[].distance")
                                        .type(JsonFieldType.NUMBER)
                                        .description("기준 위치로부터 거리(km)")
                                        .optional(),
                                fieldWithPath("data.recommendedMenus[].distanceText")
                                        .type(JsonFieldType.STRING)
                                        .description("거리 정보 텍스트")
                                        .optional(),
                                fieldWithPath("data.recommendedMenus[].tags")
                                        .type(JsonFieldType.ARRAY)
                                        .description("메뉴 태그 목록"),
                                fieldWithPath("data.recommendedMenus[].tag")
                                        .type(JsonFieldType.STRING)
                                        .description("대표 태그")
                                        .optional(),
                                fieldWithPath("data.recommendedMenus[].imageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("메뉴 이미지 URL")
                                        .optional(),
                                fieldWithPath("data.recommendedStores")
                                        .type(JsonFieldType.ARRAY)
                                        .description("추천 가게 목록"),
                                fieldWithPath("data.recommendedStores[].storeId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 ID"),
                                fieldWithPath("data.recommendedStores[].storeName")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 이름"),
                                fieldWithPath("data.recommendedStores[].categoryName")
                                        .type(JsonFieldType.STRING)
                                        .description("대표 카테고리명"),
                                fieldWithPath("data.recommendedStores[].distance")
                                        .type(JsonFieldType.NUMBER)
                                        .description("기준 위치로부터 거리(km)")
                                        .optional(),
                                fieldWithPath("data.recommendedStores[].distanceText")
                                        .type(JsonFieldType.STRING)
                                        .description("거리 정보 텍스트")
                                        .optional(),
                                fieldWithPath("data.recommendedStores[].contextInfo")
                                        .type(JsonFieldType.STRING)
                                        .description("위치 설명")
                                        .optional(),
                                fieldWithPath("data.recommendedStores[].averagePrice")
                                        .type(JsonFieldType.NUMBER)
                                        .description("평균 가격")
                                        .optional(),
                                fieldWithPath("data.recommendedStores[].reviewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("리뷰 수")
                                        .optional(),
                                fieldWithPath("data.recommendedStores[].imageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 이미지 URL")
                                        .optional(),
                                fieldWithPath("data.recommendedStores[].tag")
                                        .type(JsonFieldType.STRING)
                                        .description("추천 태그")
                                        .optional(),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("온보딩 상태 조회 성공")
    void getOnboardingStatus_success_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/me/onboarding-status")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isOnboardingComplete").value(true))
                .andDo(document("home/onboarding-status-get-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("온보딩 상태 데이터"),
                                fieldWithPath("data.isOnboardingComplete")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("온보딩 완료 여부"),
                                fieldWithPath("data.hasSelectedRecommendationType")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("추천 유형 선택 여부"),
                                fieldWithPath("data.hasConfirmedMonthlyBudget")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("월별 예산 확인 여부"),
                                fieldWithPath("data.currentMonth")
                                        .type(JsonFieldType.STRING)
                                        .description("현재 연월 (YYYY-MM)"),
                                fieldWithPath("data.showRecommendationTypeModal")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("추천 유형 선택 모달 표시 여부"),
                                fieldWithPath("data.showMonthlyBudgetModal")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("월별 예산 확인 모달 표시 여부"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("월별 예산 확인 처리 성공")
    void confirmMonthlyBudget_success_docs() throws Exception {
        // given
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        MonthlyBudgetConfirmRequest request = new MonthlyBudgetConfirmRequest(
                year,
                month,
                "KEEP"
        );

        // when & then
        mockMvc.perform(post("/api/v1/members/me/monthly-budget-confirmed")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.year").value(year))
                .andExpect(jsonPath("$.data.month").value(month))
                .andDo(document("home/monthly-budget-confirm-post-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        requestFields(
                                fieldWithPath("year")
                                        .type(JsonFieldType.NUMBER)
                                        .description("연도"),
                                fieldWithPath("month")
                                        .type(JsonFieldType.NUMBER)
                                        .description("월 (1-12)"),
                                fieldWithPath("action")
                                        .type(JsonFieldType.STRING)
                                        .description("사용자 액션 (KEEP: 유지, CHANGE: 변경)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("월별 예산 확인 결과 데이터"),
                                fieldWithPath("data.year")
                                        .type(JsonFieldType.NUMBER)
                                        .description("연도"),
                                fieldWithPath("data.month")
                                        .type(JsonFieldType.NUMBER)
                                        .description("월"),
                                fieldWithPath("data.confirmedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("확인 시각 (ISO 8601)"),
                                fieldWithPath("data.monthlyBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("월별 예산 금액"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("월별 예산 확인 처리 실패 - 잘못된 액션")
    void confirmMonthlyBudget_fail_invalidAction_docs() throws Exception {
        // given
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        MonthlyBudgetConfirmRequest request = new MonthlyBudgetConfirmRequest(
                year,
                month,
                "INVALID"
        );

        // when & then
        mockMvc.perform(post("/api/v1/members/me/monthly-budget-confirmed")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("home/monthly-budget-confirm-post-fail-invalid-action",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        requestFields(
                                fieldWithPath("year")
                                        .type(JsonFieldType.NUMBER)
                                        .description("연도"),
                                fieldWithPath("month")
                                        .type(JsonFieldType.NUMBER)
                                        .description("월 (1-12)"),
                                fieldWithPath("action")
                                        .type(JsonFieldType.STRING)
                                        .description("잘못된 액션 값")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .description("데이터 (에러 시 null)")
                                        .optional(),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("상세 에러 정보"),
                                fieldWithPath("error.data.field")
                                        .type(JsonFieldType.STRING)
                                        .description("검증 실패한 필드명"),
                                fieldWithPath("error.data.reason")
                                        .type(JsonFieldType.STRING)
                                        .description("검증 실패 사유")
                        )
                ));
    }
}
