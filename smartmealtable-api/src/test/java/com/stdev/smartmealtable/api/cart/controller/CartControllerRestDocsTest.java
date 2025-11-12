package com.stdev.smartmealtable.api.cart.controller;

import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.api.budget.service.DailyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.MonthlyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.dto.DailyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.api.budget.service.dto.MonthlyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.expenditure.service.CreateExpenditureService;
import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceResponse;
import com.stdev.smartmealtable.domain.cart.Cart;
import com.stdev.smartmealtable.domain.cart.CartItem;
import com.stdev.smartmealtable.domain.cart.CartRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 장바구니 API REST Docs 테스트
 */
@DisplayName("CartController REST Docs 테스트")
@Transactional
class CartControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository authenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CreateExpenditureService createExpenditureService;
    
    @Autowired
    private DailyBudgetQueryService dailyBudgetQueryService;
    
    @Autowired
    private MonthlyBudgetQueryService monthlyBudgetQueryService;

    private Member member;
    private String accessToken;
    private Store store;
    private Food food1;
    private Food food2;


    @BeforeEach
    void setUp() {
        // 테스트 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, Address.of("테스트대학교", null, "서울특별시", null, null, null, null));
        Group savedGroup = groupRepository.save(testGroup);

        // 테스트 회원 생성
        member = Member.create(savedGroup.getGroupId(), "장바구니테스트회원", null, RecommendationType.BALANCED);
        member = memberRepository.save(member);

        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(), "cart@example.com", "hashedPassword", "장바구니테스트회원"
        );
        authenticationRepository.save(auth);

        // JWT 토큰 생성
        accessToken = createAccessToken(member.getMemberId());

        // 테스트 가게 생성
        store = Store.builder()
                .name("맛있는 음식점")
                .categoryIds(java.util.List.of(1L))
                .sellerId(1L)
                .address("서울특별시 강남구 테헤란로 100")
                .lotNumberAddress("서울특별시 강남구 역삼동 100-10")
                .latitude(new BigDecimal("37.5015678"))
                .longitude(new BigDecimal("127.0395432"))
                .phoneNumber("02-1234-5678")
                .description("맛있는 음식을 파는 가게")
                .averagePrice(8000)
                .reviewCount(100)
                .viewCount(500)
                .favoriteCount(20)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store.jpg")
                .registeredAt(LocalDateTime.now().minusMonths(1))
                .build();
        store = storeRepository.save(store);

        // 테스트 음식 1 생성 (reconstitute 패턴)
        food1 = Food.reconstitute(
                null,  // foodId는 save 후 자동 생성
                "김치찌개",
                store.getStoreId(),  // storeId
                1L,  // categoryId
                "맛있는 김치찌개",
                "https://example.com/food1.jpg",
                8000
        );
        food1 = foodRepository.save(food1);

        // 테스트 음식 2 생성 (reconstitute 패턴)
        food2 = Food.reconstitute(
                null,  // foodId는 save 후 자동 생성
                "된장찌개",
                store.getStoreId(),  // storeId
                1L,  // categoryId
                "맛있는 된장찌개",
                "https://example.com/food2.jpg",
                7000
        );
        food2 = foodRepository.save(food2);
    }

    @Test
    @DisplayName("장바구니 아이템 추가 성공")
    void addCartItem_success_docs() throws Exception {
        // given
        String requestBody = """
                {
                    "storeId": %d,
                    "foodId": %d,
                    "quantity": 2
                }
                """.formatted(store.getStoreId(), food1.getFoodId());

        // when & then
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andDo(document("cart-add-item-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)"),
                                headerWithName("Content-Type").description("application/json")
                        ),
                        requestFields(
                                fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("foodId").type(JsonFieldType.NUMBER).description("음식 ID"),
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("수량"),
                                fieldWithPath("replaceCart").type(JsonFieldType.BOOLEAN).description("기존 장바구니 내용 교체 여부 (다른 가게의 상품이 있을 때 장바구니 초기화할지 여부)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가된 아이템 정보"),
                                fieldWithPath("data.cartId").type(JsonFieldType.NUMBER).description("장바구니 ID"),
                                fieldWithPath("data.cartItemId").type(JsonFieldType.NUMBER).description("장바구니 아이템 ID"),
                                fieldWithPath("data.foodId").type(JsonFieldType.NUMBER).description("음식 ID"),
                                fieldWithPath("data.quantity").type(JsonFieldType.NUMBER).description("수량"),
                                fieldWithPath("data.replacedCart").type(JsonFieldType.BOOLEAN).description("기존 장바구니가 교체되었는지 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 아이템 추가 실패 - 인증 실패")
    void addCartItem_unauthorized_docs() throws Exception {
        // given
        String requestBody = """
                {
                    "storeId": %d,
                    "foodId": %d,
                    "quantity": 2
                }
                """.formatted(store.getStoreId(), food1.getFoodId());

        // when & then
        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"))
                .andDo(document("cart-add-item-unauthorized",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("foodId").type(JsonFieldType.NUMBER).description("음식 ID"),
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("수량"),
                                fieldWithPath("replaceCart").type(JsonFieldType.BOOLEAN).description("기존 장바구니 내용 교체 여부 (다른 가게의 상품이 있을 때 장바구니 초기화할지 여부)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E401)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("특정 가게의 장바구니 조회 성공")
    void getCart_success_docs() throws Exception {
        // given - 장바구니에 아이템 미리 추가
        Cart cart = Cart.create(member.getMemberId(), store.getStoreId());
        cart.addItem(food1.getFoodId(), 2);
        cart.addItem(food2.getFoodId(), 1);
        cart = cartRepository.save(cart);

        // when & then
        mockMvc.perform(get("/api/v1/cart/store/{storeId}", store.getStoreId())
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andDo(document("cart-get-by-store-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)")
                        ),
                        pathParameters(
                                parameterWithName("storeId").description("가게 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("장바구니 정보"),
                                fieldWithPath("data.cartId").type(JsonFieldType.NUMBER).description("장바구니 ID"),
                                fieldWithPath("data.storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING).description("가게 이름"),
                                fieldWithPath("data.items").type(JsonFieldType.ARRAY).description("장바구니 아이템 목록"),
                                fieldWithPath("data.items[].cartItemId").type(JsonFieldType.NUMBER).description("장바구니 아이템 ID"),
                                fieldWithPath("data.items[].foodId").type(JsonFieldType.NUMBER).description("음식 ID"),
                                fieldWithPath("data.items[].foodName").type(JsonFieldType.STRING).description("음식 이름"),
                                fieldWithPath("data.items[].imageUrl").type(JsonFieldType.STRING).description("음식 이미지 URL"),
                                fieldWithPath("data.items[].averagePrice").type(JsonFieldType.NUMBER).description("평균 가격"),
                                fieldWithPath("data.items[].quantity").type(JsonFieldType.NUMBER).description("수량"),
                                fieldWithPath("data.items[].subtotal").type(JsonFieldType.NUMBER).description("소계 (가격 × 수량)"),
                                fieldWithPath("data.totalQuantity").type(JsonFieldType.NUMBER).description("총 아이템 개수 (수량의 합)"),
                                fieldWithPath("data.totalAmount").type(JsonFieldType.NUMBER).description("총 금액"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("전체 장바구니 조회 성공")
    void getAllCarts_success_docs() throws Exception {
        // given - 장바구니에 아이템 미리 추가
        Cart cart = Cart.create(member.getMemberId(), store.getStoreId());
        cart.addItem(food1.getFoodId(), 2);
        cart = cartRepository.save(cart);

        // when & then
        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(document("cart-get-all-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("장바구니 목록"),
                                fieldWithPath("data[].cartId").type(JsonFieldType.NUMBER).description("장바구니 ID"),
                                fieldWithPath("data[].storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("data[].storeName").type(JsonFieldType.STRING).description("가게 이름"),
                                fieldWithPath("data[].items").type(JsonFieldType.ARRAY).description("장바구니 아이템 목록"),
                                fieldWithPath("data[].items[].cartItemId").type(JsonFieldType.NUMBER).description("장바구니 아이템 ID"),
                                fieldWithPath("data[].items[].foodId").type(JsonFieldType.NUMBER).description("음식 ID"),
                                fieldWithPath("data[].items[].foodName").type(JsonFieldType.STRING).description("음식 이름"),
                                fieldWithPath("data[].items[].imageUrl").type(JsonFieldType.STRING).description("음식 이미지 URL"),
                                fieldWithPath("data[].items[].averagePrice").type(JsonFieldType.NUMBER).description("평균 가격"),
                                fieldWithPath("data[].items[].quantity").type(JsonFieldType.NUMBER).description("수량"),
                                fieldWithPath("data[].items[].subtotal").type(JsonFieldType.NUMBER).description("소계 (가격 × 수량)"),
                                fieldWithPath("data[].totalQuantity").type(JsonFieldType.NUMBER).description("총 아이템 개수 (수량의 합)"),
                                fieldWithPath("data[].totalAmount").type(JsonFieldType.NUMBER).description("총 금액"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 아이템 수량 수정 성공")
    void updateCartItemQuantity_success_docs() throws Exception {
        // given - 장바구니에 아이템 미리 추가
        Cart cart = Cart.create(member.getMemberId(), store.getStoreId());
        cart.addItem(food1.getFoodId(), 2);
        cart = cartRepository.save(cart);
        
        // CartItem ID 획득 (Aggregate 내부의 첫 번째 아이템)
        Long cartItemId = cart.getItems().get(0).getCartItemId();

        String requestBody = """
                {
                    "quantity": 5
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("cart-update-item-quantity-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)"),
                                headerWithName("Content-Type").description("application/json")
                        ),
                        pathParameters(
                                parameterWithName("cartItemId").description("장바구니 아이템 ID")
                        ),
                        requestFields(
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("새로운 수량")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (수정 성공 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 아이템 수량 수정 실패 - 존재하지 않는 아이템")
    void updateCartItemQuantity_notFound_docs() throws Exception {
        // given
        String requestBody = """
                {
                    "quantity": 5
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/cart/items/{cartItemId}", 99999L)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andDo(document("cart-update-item-quantity-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)"),
                                headerWithName("Content-Type").description("application/json")
                        ),
                        pathParameters(
                                parameterWithName("cartItemId").description("장바구니 아이템 ID (존재하지 않는 ID)")
                        ),
                        requestFields(
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("새로운 수량")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E404)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 성공")
    void removeCartItem_success_docs() throws Exception {
        // given - 장바구니에 아이템 미리 추가
        Cart cart = Cart.create(member.getMemberId(), store.getStoreId());
        cart.addItem(food1.getFoodId(), 2);
        cart = cartRepository.save(cart);
        
        // CartItem ID 획득
        Long cartItemId = cart.getItems().get(0).getCartItemId();

        // when & then
        mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("cart-remove-item-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)")
                        ),
                        pathParameters(
                                parameterWithName("cartItemId").description("장바구니 아이템 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (삭제 성공 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("특정 가게의 장바구니 전체 삭제 성공")
    void clearCart_success_docs() throws Exception {
        // given - 장바구니에 아이템 미리 추가
        Cart cart = Cart.create(member.getMemberId(), store.getStoreId());
        cart.addItem(food1.getFoodId(), 2);
        cart.addItem(food2.getFoodId(), 1);
        cart = cartRepository.save(cart);

        // when & then
        mockMvc.perform(delete("/api/v1/cart/store/{storeId}", store.getStoreId())
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("cart-clear-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)")
                        ),
                        pathParameters(
                                parameterWithName("storeId").description("가게 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (삭제 성공 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 전체 삭제 성공 - 존재하지 않는 장바구니도 성공 처리")
    void clearCart_success_nonExistent_docs() throws Exception {
        // given - 존재하지 않는 장바구니 (아무것도 생성하지 않음)
        
        // when & then - 존재하지 않아도 200 OK (멱등성 보장)
        mockMvc.perform(delete("/api/v1/cart/store/{storeId}", 99999L)
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("cart-clear-non-existent",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)")
                        ),
                        pathParameters(
                                parameterWithName("storeId").description("가게 ID (존재하지 않는 장바구니)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (SUCCESS) - 존재하지 않아도 성공"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (삭제 성공 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 결제 성공 - 할인 포함")
    void checkoutCart_withDiscount_success_docs() throws Exception {
        // given - CreateExpenditureService mock 설정
        CreateExpenditureServiceResponse mockResponse = new CreateExpenditureServiceResponse(
                100L,  // expenditureId
                store.getStoreId(),  // storeId
                store.getName(),  // storeName
                23000,  // amount (8000*2 + 7000 - 1000)
                java.time.LocalDate.parse("2025-01-10"),  // expendedDate
                java.time.LocalTime.parse("09:30:00"),  // expendedTime
                null,  // categoryId
                null,  // categoryName
                com.stdev.smartmealtable.domain.expenditure.MealType.BREAKFAST,  // mealType
                "맛있게 먹었습니다",  // memo
                1000L,  // discount
                java.util.List.of(),  // items
                LocalDateTime.now()  // createdAt
        );
        Mockito.when(createExpenditureService.createExpenditure(Mockito.any()))
                .thenReturn(mockResponse);
        
        // given - DailyBudgetQueryService mock 설정
        DailyBudgetQueryServiceResponse dailyBudgetMock = new DailyBudgetQueryServiceResponse(
                java.time.LocalDate.parse("2025-01-10"),  // date
                50000,  // totalBudget
                27000,  // totalSpent (before)
                23000,  // remainingBudget (before)
                java.util.List.of(
                        new DailyBudgetQueryServiceResponse.MealBudgetInfo(
                                com.stdev.smartmealtable.domain.expenditure.MealType.BREAKFAST,
                                15000,  // budget
                                2000,  // spent (before)
                                13000  // remaining (before)
                        )
                )
        );
        Mockito.when(dailyBudgetQueryService.getDailyBudget(Mockito.anyLong(), Mockito.any()))
                .thenReturn(dailyBudgetMock);
        
        // given - MonthlyBudgetQueryService mock 설정
        MonthlyBudgetQueryServiceResponse monthlyBudgetMock = new MonthlyBudgetQueryServiceResponse(
                2025,  // year
                1,  // month
                1500000,  // totalBudget
                500000,  // totalSpent (before)
                1000000,  // remainingBudget (before)
                new java.math.BigDecimal("33.33"),  // utilizationRate
                21  // daysRemaining
        );
        Mockito.when(monthlyBudgetQueryService.getMonthlyBudget(Mockito.any()))
                .thenReturn(monthlyBudgetMock);
        
        // given - 장바구니에 아이템 추가
        Cart cart = Cart.create(member.getMemberId(), store.getStoreId());
        cart.addItem(food1.getFoodId(), 2);
        cart.addItem(food2.getFoodId(), 1);
        cartRepository.save(cart);

        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "storeId", store.getStoreId(),
                        "mealType", "BREAKFAST",
                        "discount", 1000L,
                        "expendedDate", "2025-01-10",
                        "expendedTime", "09:30:00",
                        "memo", "맛있게 먹었습니다"
                )
        );

        // when & then
        mockMvc.perform(post("/api/v1/cart/checkout")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeName").value(store.getName()))
                .andExpect(jsonPath("$.data.mealType").value("BREAKFAST"))
                .andExpect(jsonPath("$.data.discount").value(1000))
                .andExpect(jsonPath("$.data.expenditureId").value(100))
                .andDo(document("cart-checkout-with-discount",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("mealType").type(JsonFieldType.STRING).description("식사 타입 (BREAKFAST/LUNCH/DINNER)"),
                                fieldWithPath("discount").type(JsonFieldType.NUMBER).description("할인 금액 (0 이상)"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING).description("지출 날짜 (YYYY-MM-DD)"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING).description("지출 시간 (HH:mm:ss)"),
                                fieldWithPath("memo").type(JsonFieldType.STRING).description("메모 (선택사항)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (SUCCESS)"),
                                fieldWithPath("data.expenditureId").type(JsonFieldType.NUMBER).description("지출 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING).description("가게명"),
                                fieldWithPath("data.mealType").type(JsonFieldType.STRING).description("식사 타입"),
                                fieldWithPath("data.items[].foodName").type(JsonFieldType.STRING).description("음식명"),
                                fieldWithPath("data.items[].quantity").type(JsonFieldType.NUMBER).description("수량"),
                                fieldWithPath("data.items[].price").type(JsonFieldType.NUMBER).description("가격"),
                                fieldWithPath("data.subtotal").type(JsonFieldType.NUMBER).description("소계"),
                                fieldWithPath("data.discount").type(JsonFieldType.NUMBER).description("할인 금액"),
                                fieldWithPath("data.finalAmount").type(JsonFieldType.NUMBER).description("최종 금액"),
                                fieldWithPath("data.expendedDate").type(JsonFieldType.STRING).description("지출 날짜"),
                                fieldWithPath("data.expendedTime").type(JsonFieldType.STRING).description("지출 시간"),
                                fieldWithPath("data.budgetSummary.mealBudgetBefore").type(JsonFieldType.NUMBER).description("식사별 예산 (이전)"),
                                fieldWithPath("data.budgetSummary.mealBudgetAfter").type(JsonFieldType.NUMBER).description("식사별 예산 (이후)"),
                                fieldWithPath("data.budgetSummary.dailyBudgetBefore").type(JsonFieldType.NUMBER).description("일일 예산 (이전)"),
                                fieldWithPath("data.budgetSummary.dailyBudgetAfter").type(JsonFieldType.NUMBER).description("일일 예산 (이후)"),
                                fieldWithPath("data.budgetSummary.monthlyBudgetBefore").type(JsonFieldType.NUMBER).description("월간 예산 (이전)"),
                                fieldWithPath("data.budgetSummary.monthlyBudgetAfter").type(JsonFieldType.NUMBER).description("월간 예산 (이후)"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 결제 성공 - 할인 없음")
    void checkoutCart_withoutDiscount_success_docs() throws Exception {
        // given - CreateExpenditureService mock 설정
        CreateExpenditureServiceResponse mockResponse = new CreateExpenditureServiceResponse(
                101L,  // expenditureId
                store.getStoreId(),  // storeId
                store.getName(),  // storeName
                8000,  // amount
                java.time.LocalDate.parse("2025-01-10"),  // expendedDate
                java.time.LocalTime.parse("12:00:00"),  // expendedTime
                null,  // categoryId
                null,  // categoryName
                com.stdev.smartmealtable.domain.expenditure.MealType.LUNCH,  // mealType
                null,  // memo
                0L,  // discount
                java.util.List.of(),  // items
                LocalDateTime.now()  // createdAt
        );
        Mockito.when(createExpenditureService.createExpenditure(Mockito.any()))
                .thenReturn(mockResponse);

        // given - DailyBudgetQueryService mock 설정
        DailyBudgetQueryServiceResponse dailyBudgetMock = new DailyBudgetQueryServiceResponse(
                java.time.LocalDate.parse("2025-01-10"),  // date
                50000,  // totalBudget
                8000,  // totalSpent (before)
                42000,  // remainingBudget (before)
                java.util.List.of(
                        new DailyBudgetQueryServiceResponse.MealBudgetInfo(
                                com.stdev.smartmealtable.domain.expenditure.MealType.LUNCH,
                                20000,  // budget
                                0,  // spent (before)
                                20000  // remaining (before)
                        )
                )
        );
        Mockito.when(dailyBudgetQueryService.getDailyBudget(Mockito.anyLong(), Mockito.any()))
                .thenReturn(dailyBudgetMock);
        
        // given - MonthlyBudgetQueryService mock 설정
        MonthlyBudgetQueryServiceResponse monthlyBudgetMock = new MonthlyBudgetQueryServiceResponse(
                2025,  // year
                1,  // month
                1500000,  // totalBudget
                500000,  // totalSpent (before)
                1000000,  // remainingBudget (before)
                new java.math.BigDecimal("33.33"),  // utilizationRate
                21  // daysRemaining
        );
        Mockito.when(monthlyBudgetQueryService.getMonthlyBudget(Mockito.any()))
                .thenReturn(monthlyBudgetMock);
        
        // given - 장바구니에 아이템 추가
        Cart cart = Cart.create(member.getMemberId(), store.getStoreId());
        cart.addItem(food1.getFoodId(), 1);
        cartRepository.save(cart);

        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "storeId", store.getStoreId(),
                        "mealType", "LUNCH",
                        "discount", 0L,
                        "expendedDate", "2025-01-10",
                        "expendedTime", "12:00:00"
                )
        );

        // when & then
        mockMvc.perform(post("/api/v1/cart/checkout")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.discount").value(0))
                .andExpect(jsonPath("$.data.expenditureId").value(101))
                .andDo(document("cart-checkout-without-discount",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("mealType").type(JsonFieldType.STRING).description("식사 타입"),
                                fieldWithPath("discount").type(JsonFieldType.NUMBER).description("할인 금액 (0)"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING).description("지출 날짜"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING).description("지출 시간"),
                                fieldWithPath("memo").type(JsonFieldType.STRING).description("메모 (선택사항)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (SUCCESS)"),
                                fieldWithPath("data.expenditureId").type(JsonFieldType.NUMBER).description("지출 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING).description("가게명"),
                                fieldWithPath("data.mealType").type(JsonFieldType.STRING).description("식사 타입"),
                                fieldWithPath("data.items[].foodName").type(JsonFieldType.STRING).description("음식명"),
                                fieldWithPath("data.items[].quantity").type(JsonFieldType.NUMBER).description("수량"),
                                fieldWithPath("data.items[].price").type(JsonFieldType.NUMBER).description("가격"),
                                fieldWithPath("data.subtotal").type(JsonFieldType.NUMBER).description("소계"),
                                fieldWithPath("data.discount").type(JsonFieldType.NUMBER).description("할인 금액"),
                                fieldWithPath("data.finalAmount").type(JsonFieldType.NUMBER).description("최종 금액"),
                                fieldWithPath("data.expendedDate").type(JsonFieldType.STRING).description("지출 날짜"),
                                fieldWithPath("data.expendedTime").type(JsonFieldType.STRING).description("지출 시간"),
                                fieldWithPath("data.budgetSummary.mealBudgetBefore").type(JsonFieldType.NUMBER).description("식사별 예산 (이전)"),
                                fieldWithPath("data.budgetSummary.mealBudgetAfter").type(JsonFieldType.NUMBER).description("식사별 예산 (이후)"),
                                fieldWithPath("data.budgetSummary.dailyBudgetBefore").type(JsonFieldType.NUMBER).description("일일 예산 (이전)"),
                                fieldWithPath("data.budgetSummary.dailyBudgetAfter").type(JsonFieldType.NUMBER).description("일일 예산 (이후)"),
                                fieldWithPath("data.budgetSummary.monthlyBudgetBefore").type(JsonFieldType.NUMBER).description("월간 예산 (이전)"),
                                fieldWithPath("data.budgetSummary.monthlyBudgetAfter").type(JsonFieldType.NUMBER).description("월간 예산 (이후)"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 결제 실패 - 할인 금액이 총액을 초과")
    void checkoutCart_discountExceedsTotal_docs() throws Exception {
        // given - 장바구니에 아이템 추가
        Cart cart = Cart.create(member.getMemberId(), store.getStoreId());
        cart.addItem(food1.getFoodId(), 1);  // 가격: 10000원
        cartRepository.save(cart);

        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "storeId", store.getStoreId(),
                        "mealType", "BREAKFAST",
                        "discount", 20000L,  // 총액보다 큰 할인
                        "expendedDate", "2025-01-10",
                        "expendedTime", "09:30:00"
                )
        );

        // when & then
        mockMvc.perform(post("/api/v1/cart/checkout")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("cart-checkout-discount-exceeds-total",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT Access Token (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("mealType").type(JsonFieldType.STRING).description("식사 타입"),
                                fieldWithPath("discount").type(JsonFieldType.NUMBER).description("할인 금액 (총액 초과)"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING).description("지출 날짜"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING).description("지출 시간"),
                                fieldWithPath("memo").type(JsonFieldType.STRING).description("메모").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("전체 장바구니 비우기 성공")
    void clearAllCarts_success_docs() throws Exception {
        // given - 장바구니에 아이템 추가
        Cart cart = Cart.create(member.getMemberId(), store.getStoreId());
        cart.addItem(food1.getFoodId(), 2);
        cartRepository.save(cart);

        System.out.println("cart.getCartId() = " + cart.getCartId());
        System.out.println("cart.getMemberId() = " + cart.getMemberId());

        // when & then
        mockMvc.perform(delete("/api/v1/cart")
                        .header("Authorization", accessToken))
                .andExpect(status().isNoContent());
    }
}
