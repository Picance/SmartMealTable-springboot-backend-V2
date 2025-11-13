package com.stdev.smartmealtable.api.favorite.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.favorite.dto.AddFavoriteRequest;
import com.stdev.smartmealtable.api.favorite.dto.ReorderFavoritesRequest;
import com.stdev.smartmealtable.api.home.service.BusinessHoursService;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.domain.favorite.Favorite;
import com.stdev.smartmealtable.domain.favorite.FavoriteRepository;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 즐겨찾기 API 컨트롤러 통합 테스트
 */
@DisplayName("즐겨찾기 API 테스트")
class FavoriteControllerTest extends AbstractRestDocsTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AddressHistoryRepository addressHistoryRepository;

    @MockBean
    private BusinessHoursService businessHoursService;

    private Long memberId;
    private Long storeId1;
    private Long storeId2;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 1. 카테고리 생성
        Category category = Category.reconstitute(null, "한식");
        Category savedCategory = categoryRepository.save(category);

        // 2. 회원 생성 (먼저 생성)
        Member member = Member.create(null, "테스트유저", null, RecommendationType.BALANCED);
        member = memberRepository.save(member);
        memberId = member.getMemberId();

        // 3. 회원 인증 정보 생성 (회원 ID 연결)
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                memberId,
                "test@example.com",
                "encodedPassword123!",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);

        // 4. 기본 주소 등록
        Address homeAddress = Address.of(
                "우리집",
                "서울특별시 강남구 테헤란로 1",
                "서울특별시 강남구 테헤란로 1",
                "101호",
                37.5665,
                126.9780,
                AddressType.HOME
        );
        AddressHistory addressHistory = AddressHistory.create(memberId, homeAddress, true);
        addressHistoryRepository.save(addressHistory);

        // 5. 가게 생성
        Store store1 = Store.builder()
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
                .imageUrl("https://example.com/store1.jpg")
                .registeredAt(LocalDateTime.now())
                .build();
        Store savedStore1 = storeRepository.save(store1);
        storeId1 = savedStore1.getStoreId();

        Store store2 = Store.builder()
                .name("맛있는 중식집")
                .categoryIds(java.util.List.of(savedCategory.getCategoryId()))
                .sellerId(2L)
                .address("서울시 강남구 테헤란로 456")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .phoneNumber("02-1234-5679")
                .description("정통 중식을 맛볼 수 있는 곳")
                .averagePrice(15000)
                .reviewCount(200)
                .viewCount(1500)
                .favoriteCount(80)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store2.jpg")
                .registeredAt(LocalDateTime.now())
                .build();
        Store savedStore2 = storeRepository.save(store2);
        storeId2 = savedStore2.getStoreId();

        when(businessHoursService.getOperationStatus(storeId1))
                .thenReturn(new BusinessHoursService.StoreOperationStatus("영업중", true));
        when(businessHoursService.getOperationStatus(storeId2))
                .thenReturn(new BusinessHoursService.StoreOperationStatus("휴무", false));

        // 5. JWT 토큰 생성
        accessToken = createAccessToken(memberId);
    }

    @Test
    @DisplayName("즐겨찾기 추가 - 성공")
    void addFavorite_success() throws Exception {
        // given
        AddFavoriteRequest request = new AddFavoriteRequest(storeId1);

        // when & then
        mockMvc.perform(post("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())  // 응답 출력
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.favoriteId").exists())
                .andExpect(jsonPath("$.data.storeId").value(storeId1))
                .andExpect(jsonPath("$.data.priority").value(1))
                .andExpect(jsonPath("$.data.favoritedAt").exists())
                .andDo(document("favorite-add-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("storeId").description("즐겨찾기할 가게 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data.favoriteId").description("생성된 즐겨찾기 ID"),
                                fieldWithPath("data.storeId").description("가게 ID"),
                                fieldWithPath("data.priority").description("즐겨찾기 순서"),
                                fieldWithPath("data.favoritedAt").description("즐겨찾기 등록 시각"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("즐겨찾기 추가 - 실패: 존재하지 않는 가게")
    void addFavorite_fail_storeNotFound() throws Exception {
        // given
        Long nonExistentStoreId = 99999L;
        AddFavoriteRequest request = new AddFavoriteRequest(nonExistentStoreId);

        // when & then
        mockMvc.perform(post("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 가게입니다."));
    }

    @Test
    @DisplayName("즐겨찾기 추가 - 실패: 중복 등록")
    void addFavorite_fail_duplicate() throws Exception {
        // given: 이미 즐겨찾기 등록되어 있음
        Favorite existing = Favorite.create(memberId, storeId1, 1L);
        favoriteRepository.save(existing);

        AddFavoriteRequest request = new AddFavoriteRequest(storeId1);

        // when & then
        mockMvc.perform(post("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E409"))
                .andExpect(jsonPath("$.error.message").value("이미 즐겨찾기에 추가된 가게입니다."));
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 성공")
    void getFavorites_success() throws Exception {
        // given: 2개의 즐겨찾기 등록
        Favorite favorite1 = Favorite.create(memberId, storeId1, 1L);
        Favorite favorite2 = Favorite.create(memberId, storeId2, 2L);
        favoriteRepository.save(favorite1);
        favoriteRepository.save(favorite2);

        // when & then
        mockMvc.perform(get("/api/v1/favorites")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.favorites").isArray())
                .andExpect(jsonPath("$.data.favorites.length()").value(2))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.openCount").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.favorites[0].favoriteId").exists())
                .andExpect(jsonPath("$.data.favorites[0].storeId").value(storeId1))
                .andExpect(jsonPath("$.data.favorites[0].storeName").value("맛있는 한식집"))
                .andExpect(jsonPath("$.data.favorites[0].displayOrder").value(1))
                .andExpect(jsonPath("$.data.favorites[0].distance").isNumber())
                .andExpect(jsonPath("$.data.favorites[0].isOpenNow").value(true))
                .andDo(document("favorite-list-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data.favorites[]").description("즐겨찾기 목록"),
                                fieldWithPath("data.favorites[].favoriteId").description("즐겨찾기 ID"),
                                fieldWithPath("data.favorites[].storeId").description("가게 ID"),
                                fieldWithPath("data.favorites[].storeName").description("가게명"),
                                fieldWithPath("data.favorites[].categoryId").description("대표 카테고리 ID").optional(),
                                fieldWithPath("data.favorites[].categoryName").description("카테고리명"),
                                fieldWithPath("data.favorites[].reviewCount").description("리뷰 수"),
                                fieldWithPath("data.favorites[].averagePrice").description("평균 가격"),
                                fieldWithPath("data.favorites[].address").description("가게 주소"),
                                fieldWithPath("data.favorites[].distance").description("사용자 기준 거리(km)").optional(),
                                fieldWithPath("data.favorites[].imageUrl").description("가게 이미지 URL").optional(),
                                fieldWithPath("data.favorites[].displayOrder").description("즐겨찾기 정렬 순서"),
                                fieldWithPath("data.favorites[].isOpenNow").description("현재 영업 중 여부"),
                                fieldWithPath("data.favorites[].createdAt").description("즐겨찾기 등록 시각"),
                                fieldWithPath("data.totalCount").description("필터 조건을 만족하는 전체 즐겨찾기 개수"),
                                fieldWithPath("data.openCount").description("현재 영업 중인 즐겨찾기 수"),
                                fieldWithPath("data.size").description("요청한 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                fieldWithPath("data.nextCursor").description("다음 페이지 커서 (없으면 null)").optional(),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 빈 목록")
    void getFavorites_empty() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/favorites")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.favorites").isEmpty())
                .andExpect(jsonPath("$.data.totalCount").value(0))
                .andExpect(jsonPath("$.data.openCount").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()));
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 커서 & 영업중 필터")
    void getFavorites_cursorAndOpenFilter() throws Exception {
        Favorite first = favoriteRepository.save(Favorite.create(memberId, storeId1, 1L));
        Favorite second = favoriteRepository.save(Favorite.create(memberId, storeId2, 2L));

        // 페이지 크기 1로 첫 요청
        mockMvc.perform(get("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorites.length()").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(first.getFavoriteId().intValue()));

        // 커서를 전달하여 다음 데이터 조회
        mockMvc.perform(get("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .param("size", "1")
                        .param("cursor", String.valueOf(first.getFavoriteId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorites.length()").value(1))
                .andExpect(jsonPath("$.data.favorites[0].storeId").value(storeId2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()));

        // 영업 중인 가게만 필터
        mockMvc.perform(get("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .param("isOpenOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.favorites.length()").value(1))
                .andExpect(jsonPath("$.data.favorites[0].storeId").value(storeId1))
                .andExpect(jsonPath("$.data.openCount").value(1));
    }

    @Test
    @DisplayName("즐겨찾기 순서 변경 - 성공")
    void reorderFavorites_success() throws Exception {
        // given: 2개의 즐겨찾기 등록
        Favorite favorite1 = favoriteRepository.save(Favorite.create(memberId, storeId1, 1L));
        Favorite favorite2 = favoriteRepository.save(Favorite.create(memberId, storeId2, 2L));

        List<ReorderFavoritesRequest.FavoriteOrderDto> orderList = new ArrayList<>();
        orderList.add(new ReorderFavoritesRequest.FavoriteOrderDto(favorite2.getFavoriteId(), 1L));
        orderList.add(new ReorderFavoritesRequest.FavoriteOrderDto(favorite1.getFavoriteId(), 2L));

        ReorderFavoritesRequest request = new ReorderFavoritesRequest(orderList);

        // when & then
        mockMvc.perform(put("/api/v1/favorites/reorder")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.updatedCount").value(2))
                .andExpect(jsonPath("$.data.message").exists())
                .andDo(document("favorite-reorder-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("favoriteOrders[]").description("즐겨찾기 순서 목록"),
                                fieldWithPath("favoriteOrders[].favoriteId").description("즐겨찾기 ID"),
                                fieldWithPath("favoriteOrders[].priority").description("새로운 우선순위")
                        ),
                        responseFields(
                                fieldWithPath("result").description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data.updatedCount").description("업데이트된 즐겨찾기 개수"),
                                fieldWithPath("data.message").description("결과 메시지"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("즐겨찾기 순서 변경 - 실패: 권한 없음 (다른 사용자 즐겨찾기)")
    void reorderFavorites_fail_forbidden() throws Exception {
        // given: 다른 사용자의 즐겨찾기
        Long otherMemberId = 99999L;
        Favorite otherFavorite = favoriteRepository.save(Favorite.create(otherMemberId, storeId1, 1L));

        List<ReorderFavoritesRequest.FavoriteOrderDto> orderList = new ArrayList<>();
        orderList.add(new ReorderFavoritesRequest.FavoriteOrderDto(otherFavorite.getFavoriteId(), 1L));

        ReorderFavoritesRequest request = new ReorderFavoritesRequest(orderList);

        // when & then
        mockMvc.perform(put("/api/v1/favorites/reorder")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E403"))
                .andExpect(jsonPath("$.error.message").value("다른 사용자의 리소스에 접근할 수 없습니다."));
    }

    @Test
    @DisplayName("즐겨찾기 삭제 - 성공")
    void deleteFavorite_success() throws Exception {
        // given
        Favorite favorite = favoriteRepository.save(Favorite.create(memberId, storeId1, 1L));

        // when & then
        mockMvc.perform(delete("/api/v1/favorites/{favoriteId}", favorite.getFavoriteId())
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.favoriteId").value(favorite.getFavoriteId()))
                .andExpect(jsonPath("$.data.message").exists())
                .andDo(document("favorite-delete-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data.favoriteId").description("삭제된 즐겨찾기 ID"),
                                fieldWithPath("data.message").description("결과 메시지"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("즐겨찾기 삭제 - 실패: 존재하지 않는 즐겨찾기")
    void deleteFavorite_fail_notFound() throws Exception {
        // given
        Long nonExistentFavoriteId = 99999L;

        // when & then
        mockMvc.perform(delete("/api/v1/favorites/{favoriteId}", nonExistentFavoriteId)
                        .header("Authorization", accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 즐겨찾기입니다."));
    }

    @Test
    @DisplayName("즐겨찾기 삭제 - 실패: 권한 없음 (다른 사용자 즐겨찾기)")
    void deleteFavorite_fail_forbidden() throws Exception {
        // given: 다른 사용자의 즐겨찾기
        Long otherMemberId = 99999L;
        Favorite otherFavorite = favoriteRepository.save(Favorite.create(otherMemberId, storeId1, 1L));

        // when & then
        mockMvc.perform(delete("/api/v1/favorites/{favoriteId}", otherFavorite.getFavoriteId())
                        .header("Authorization", accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E403"))
                .andExpect(jsonPath("$.error.message").value("다른 사용자의 리소스에 접근할 수 없습니다."));
    }
}
