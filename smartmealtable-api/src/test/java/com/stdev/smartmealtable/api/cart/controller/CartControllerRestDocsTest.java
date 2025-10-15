package com.stdev.smartmealtable.api.cart.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.cart.Cart;
import com.stdev.smartmealtable.domain.cart.CartItem;
import com.stdev.smartmealtable.domain.cart.CartRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    private Member member;
    private String accessToken;
    private Store store;
    private Food food1;
    private Food food2;

    @BeforeEach
    void setUp() {
        // 테스트 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시");
        Group savedGroup = groupRepository.save(testGroup);

        // 테스트 회원 생성
        member = Member.create(savedGroup.getGroupId(), "장바구니테스트회원", RecommendationType.BALANCED);
        member = memberRepository.save(member);

        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(), "cart@example.com", "hashedPassword", "장바구니테스트회원"
        );
        authenticationRepository.save(auth);

        // JWT 토큰 생성
        accessToken = jwtTokenProvider.createToken(member.getMemberId());

        // 테스트 가게 생성
        store = Store.builder()
                .name("맛있는 음식점")
                .categoryId(1L)
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
                1L,
                "맛있는 김치찌개",
                "https://example.com/food1.jpg",
                8000
        );
        food1 = foodRepository.save(food1);

        // 테스트 음식 2 생성 (reconstitute 패턴)
        food2 = Food.reconstitute(
                null,  // foodId는 save 후 자동 생성
                "된장찌개",
                1L,
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
                        .header("Authorization", "Bearer " + accessToken)
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
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("수량")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가된 아이템 정보"),
                                fieldWithPath("data.cartId").type(JsonFieldType.NUMBER).description("장바구니 ID"),
                                fieldWithPath("data.cartItemId").type(JsonFieldType.NUMBER).description("장바구니 아이템 ID"),
                                fieldWithPath("data.foodId").type(JsonFieldType.NUMBER).description("음식 ID"),
                                fieldWithPath("data.quantity").type(JsonFieldType.NUMBER).description("수량"),
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
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("수량")
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
                        .header("Authorization", "Bearer " + accessToken))
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
                        .header("Authorization", "Bearer " + accessToken))
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
                        .header("Authorization", "Bearer " + accessToken)
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
                        .header("Authorization", "Bearer " + accessToken)
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
                        .header("Authorization", "Bearer " + accessToken))
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
                        .header("Authorization", "Bearer " + accessToken))
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
                        .header("Authorization", "Bearer " + accessToken))
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
}
