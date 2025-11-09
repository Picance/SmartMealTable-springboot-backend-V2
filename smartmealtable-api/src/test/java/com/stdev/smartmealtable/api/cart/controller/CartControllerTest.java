package com.stdev.smartmealtable.api.cart.controller;

import com.stdev.smartmealtable.api.cart.dto.AddCartItemRequest;
import com.stdev.smartmealtable.api.cart.dto.UpdateCartItemQuantityRequest;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 장바구니 API 컨트롤러 통합 테스트
 */
@DisplayName("장바구니 API 테스트")
class CartControllerTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FoodRepository foodRepository;

    private Long memberId;
    private Long storeId;
    private Long foodId1;
    private Long foodId2;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 1. 카테고리 생성
        Category category = Category.reconstitute(null, "한식");
        Category savedCategory = categoryRepository.save(category);

        // 2. 회원 생성
        Member member = Member.create(null, "테스트유저", null, RecommendationType.BALANCED);
        member = memberRepository.save(member);
        memberId = member.getMemberId();

        // 3. 회원 인증 정보 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                memberId,
                "test@example.com",
                "encodedPassword123!",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);

        // 4. 가게 생성
        Store store = Store.builder()
                .name("맛있는 한식집")
                .categoryIds(java.util.List.of(savedCategory.getCategoryId()))
                .sellerId(1L)
                .address("서울시 강남구 테헤란로 123")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .phoneNumber("02-1234-5678")
                .description("정통 한식을 맛볼 수 있는 곳")
                .averagePrice(12000)
                .reviewCount(150)
                .viewCount(1000)
                .favoriteCount(50)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store.jpg")
                .registeredAt(LocalDateTime.now())
                .build();
        Store savedStore = storeRepository.save(store);
        storeId = savedStore.getStoreId();

        // 5. 음식 생성
        Food food1 = Food.reconstitute(
                null,
                "김치찌개",
                savedStore.getStoreId(),
                savedCategory.getCategoryId(),
                "맛있는 김치찌개",
                "https://example.com/food1.jpg",
                8000
        );
        Food savedFood1 = foodRepository.save(food1);
        foodId1 = savedFood1.getFoodId();

        Food food2 = Food.reconstitute(
                null,
                "된장찌개",
                savedStore.getStoreId(),
                savedCategory.getCategoryId(),
                "맛있는 된장찌개",
                "https://example.com/food2.jpg",
                7000
        );
        Food savedFood2 = foodRepository.save(food2);
        foodId2 = savedFood2.getFoodId();

        // 6. JWT 토큰 생성
        accessToken = createAccessToken(memberId);
    }

    @Test
    @DisplayName("장바구니 아이템 추가 - 성공")
    void addCartItem_success() throws Exception {
        // given
        AddCartItemRequest request = AddCartItemRequest.builder()
                .storeId(storeId)
                .foodId(foodId1)
                .quantity(2)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.cartId").exists())
                .andExpect(jsonPath("$.data.cartItemId").exists())
                .andExpect(jsonPath("$.data.foodId").value(foodId1))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andDo(document("cart-add-item-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("storeId").description("가게 ID"),
                                fieldWithPath("foodId").description("음식 ID"),
                                fieldWithPath("quantity").description("수량")
                        ),
                        responseFields(
                                fieldWithPath("result").description("응답 결과 (SUCCESS/FAIL)"),
                                fieldWithPath("data.cartId").description("장바구니 ID"),
                                fieldWithPath("data.cartItemId").description("장바구니 아이템 ID"),
                                fieldWithPath("data.foodId").description("음식 ID"),
                                fieldWithPath("data.quantity").description("수량"),
                                subsectionWithPath("error").description("에러 정보 (성공 시 null)").optional().type("Null")
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 아이템 추가 - 잘못된 수량 (0 이하)")
    void addCartItem_invalidQuantity() throws Exception {
        // given
        AddCartItemRequest request = AddCartItemRequest.builder()
                .storeId(storeId)
                .foodId(foodId1)
                .quantity(0)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("장바구니 아이템 추가 - 존재하지 않는 가게")
    void addCartItem_storeNotFound() throws Exception {
        // given
        AddCartItemRequest request = AddCartItemRequest.builder()
                .storeId(99999L)
                .foodId(foodId1)
                .quantity(2)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    @Test
    @DisplayName("장바구니 아이템 추가 - 존재하지 않는 음식")
    void addCartItem_foodNotFound() throws Exception {
        // given
        AddCartItemRequest request = AddCartItemRequest.builder()
                .storeId(storeId)
                .foodId(99999L)
                .quantity(2)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    @Test
    @DisplayName("특정 가게의 장바구니 조회 - 성공")
    void getCart_success() throws Exception {
        // given: 먼저 아이템 추가
        AddCartItemRequest request1 = AddCartItemRequest.builder()
                .storeId(storeId)
                .foodId(foodId1)
                .quantity(2)
                .build();
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        AddCartItemRequest request2 = AddCartItemRequest.builder()
                .storeId(storeId)
                .foodId(foodId2)
                .quantity(1)
                .build();
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // when & then
        mockMvc.perform(get("/api/v1/cart/store/{storeId}", storeId)
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.cartId").exists())
                .andExpect(jsonPath("$.data.storeId").value(storeId))
                .andExpect(jsonPath("$.data.storeName").value("맛있는 한식집"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.totalQuantity").value(3))
                .andExpect(jsonPath("$.data.totalAmount").value(23000))
                .andDo(document("cart-get-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").description("응답 결과 (SUCCESS/FAIL)"),
                                fieldWithPath("data.cartId").description("장바구니 ID"),
                                fieldWithPath("data.storeId").description("가게 ID"),
                                fieldWithPath("data.storeName").description("가게 이름"),
                                fieldWithPath("data.items[]").description("장바구니 아이템 목록"),
                                fieldWithPath("data.items[].cartItemId").description("장바구니 아이템 ID"),
                                fieldWithPath("data.items[].foodId").description("음식 ID"),
                                fieldWithPath("data.items[].foodName").description("음식 이름"),
                                fieldWithPath("data.items[].imageUrl").description("음식 이미지 URL"),
                                fieldWithPath("data.items[].averagePrice").description("평균 가격"),
                                fieldWithPath("data.items[].quantity").description("수량"),
                                fieldWithPath("data.items[].subtotal").description("소계 (가격 × 수량)"),
                                fieldWithPath("data.totalQuantity").description("총 수량"),
                                fieldWithPath("data.totalAmount").description("총 금액"),
                                subsectionWithPath("error").description("에러 정보 (성공 시 null)").optional().type("Null")
                        )
                ));
    }

    @Test
    @DisplayName("특정 가게의 장바구니 조회 - 장바구니 없음")
    void getCart_notFound() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/cart/store/{storeId}", storeId)
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    @Test
    @DisplayName("내 모든 장바구니 조회 - 성공")
    void getAllCarts_success() throws Exception {
        // given: 아이템 추가
        AddCartItemRequest request = AddCartItemRequest.builder()
                .storeId(storeId)
                .foodId(foodId1)
                .quantity(2)
                .build();
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // when & then
        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andDo(document("cart-get-all-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").description("응답 결과 (SUCCESS/FAIL)"),
                                fieldWithPath("data[]").description("장바구니 목록"),
                                fieldWithPath("data[].cartId").description("장바구니 ID"),
                                fieldWithPath("data[].storeId").description("가게 ID"),
                                fieldWithPath("data[].storeName").description("가게 이름"),
                                fieldWithPath("data[].items[]").description("장바구니 아이템 목록"),
                                fieldWithPath("data[].items[].cartItemId").description("장바구니 아이템 ID"),
                                fieldWithPath("data[].items[].foodId").description("음식 ID"),
                                fieldWithPath("data[].items[].foodName").description("음식 이름"),
                                fieldWithPath("data[].items[].imageUrl").description("음식 이미지 URL"),
                                fieldWithPath("data[].items[].averagePrice").description("평균 가격"),
                                fieldWithPath("data[].items[].quantity").description("수량"),
                                fieldWithPath("data[].items[].subtotal").description("소계 (가격 × 수량)"),
                                fieldWithPath("data[].totalQuantity").description("총 수량"),
                                fieldWithPath("data[].totalAmount").description("총 금액"),
                                subsectionWithPath("error").description("에러 정보 (성공 시 null)").optional().type("Null")
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 아이템 수량 수정 - 성공")
    void updateCartItemQuantity_success() throws Exception {
        // given: 먼저 아이템 추가
        AddCartItemRequest addRequest = AddCartItemRequest.builder()
                .storeId(storeId)
                .foodId(foodId1)
                .quantity(2)
                .build();
        String addResponse = mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andReturn().getResponse().getContentAsString();

        Long cartItemId = objectMapper.readTree(addResponse).get("data").get("cartItemId").asLong();

        UpdateCartItemQuantityRequest updateRequest = UpdateCartItemQuantityRequest.builder()
                .quantity(5)
                .build();

        // when & then
        mockMvc.perform(put("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("cart-update-item-quantity-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("quantity").description("새로운 수량")
                        ),
                        responseFields(
                                fieldWithPath("result").description("응답 결과 (SUCCESS/FAIL)"),
                                subsectionWithPath("data").description("응답 데이터 (null)").optional().type("Null"),
                                subsectionWithPath("error").description("에러 정보 (성공 시 null)").optional().type("Null")
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 - 성공")
    void removeCartItem_success() throws Exception {
        // given: 먼저 아이템 추가
        AddCartItemRequest addRequest = AddCartItemRequest.builder()
                .storeId(storeId)
                .foodId(foodId1)
                .quantity(2)
                .build();
        String addResponse = mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andReturn().getResponse().getContentAsString();

        Long cartItemId = objectMapper.readTree(addResponse).get("data").get("cartItemId").asLong();

        // when & then
        mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("cart-remove-item-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").description("응답 결과 (SUCCESS/FAIL)"),
                                subsectionWithPath("data").description("응답 데이터 (null)").optional().type("Null"),
                                subsectionWithPath("error").description("에러 정보 (성공 시 null)").optional().type("Null")
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 전체 삭제 - 성공")
    void clearCart_success() throws Exception {
        // given: 먼저 아이템 추가
        AddCartItemRequest addRequest = AddCartItemRequest.builder()
                .storeId(storeId)
                .foodId(foodId1)
                .quantity(2)
                .build();
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)));

        // when & then
        mockMvc.perform(delete("/api/v1/cart/store/{storeId}", storeId)
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("cart-clear-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").description("응답 결과 (SUCCESS/FAIL)"),
                                subsectionWithPath("data").description("응답 데이터 (null)").optional().type("Null"),
                                subsectionWithPath("error").description("에러 정보 (성공 시 null)").optional().type("Null")
                        )
                ));
    }
}
