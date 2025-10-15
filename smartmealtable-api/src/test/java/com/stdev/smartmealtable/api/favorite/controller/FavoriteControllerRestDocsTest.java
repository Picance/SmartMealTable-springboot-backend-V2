package com.stdev.smartmealtable.api.favorite.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.favorite.dto.AddFavoriteRequest;
import com.stdev.smartmealtable.api.favorite.dto.ReorderFavoritesRequest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.favorite.Favorite;
import com.stdev.smartmealtable.domain.favorite.FavoriteRepository;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FavoriteController REST Docs 테스트
 */
@DisplayName("FavoriteController REST Docs")
class FavoriteControllerRestDocsTest extends AbstractRestDocsTest {
    
    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberAuthenticationRepository memberAuthenticationRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    
    private Member member;
    private String accessToken;
    private Category koreanCategory;
    private Category chineseCategory;
    private Store koreanStore;
    private Store chineseStore;
    private Favorite favorite1;
    private Favorite favorite2;
    
    @BeforeEach
    void setUpTestData() {
        // 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시 관악구");
        Group savedGroup = groupRepository.save(testGroup);
        
        // 회원 생성
        Member testMember = Member.create(savedGroup.getGroupId(), "테스트유저", RecommendationType.BALANCED);
        member = memberRepository.save(testMember);
        
        // 회원 인증 정보 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(),
                "test@example.com",
                "hashedPasswordForTest",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);
        
        // JWT 토큰 생성
        accessToken = createAccessToken(member.getMemberId());
        
        // 카테고리 생성
        koreanCategory = Category.reconstitute(null, "한식");
        koreanCategory = categoryRepository.save(koreanCategory);
        
        chineseCategory = Category.reconstitute(null, "중식");
        chineseCategory = categoryRepository.save(chineseCategory);
        
        // 가게 생성
        koreanStore = Store.builder()
                .name("맛있는 한식당")
                .categoryId(koreanCategory.getCategoryId())
                .address("서울특별시 관악구 봉천동 123")
                .lotNumberAddress("서울특별시 관악구 봉천동 123-45")
                .latitude(new BigDecimal("37.4783"))
                .longitude(new BigDecimal("126.9516"))
                .phoneNumber("02-1234-5678")
                .averagePrice(8000)
                .reviewCount(150)
                .viewCount(1200)
                .favoriteCount(50)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/korean-store.jpg")
                .registeredAt(java.time.LocalDateTime.now())
                .build();
        koreanStore = storeRepository.save(koreanStore);
        
        chineseStore = Store.builder()
                .name("중화요리 맛집")
                .categoryId(chineseCategory.getCategoryId())
                .address("서울특별시 관악구 신림동 456")
                .lotNumberAddress("서울특별시 관악구 신림동 456-78")
                .latitude(new BigDecimal("37.4845"))
                .longitude(new BigDecimal("126.9295"))
                .phoneNumber("02-8765-4321")
                .averagePrice(12000)
                .reviewCount(200)
                .viewCount(1500)
                .favoriteCount(80)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/chinese-store.jpg")
                .registeredAt(java.time.LocalDateTime.now())
                .build();
        chineseStore = storeRepository.save(chineseStore);
        
        // 즐겨찾기 생성
        favorite1 = Favorite.create(member.getMemberId(), koreanStore.getStoreId(), 1L);
        favorite1 = favoriteRepository.save(favorite1);
        
        favorite2 = Favorite.create(member.getMemberId(), chineseStore.getStoreId(), 2L);
        favorite2 = favoriteRepository.save(favorite2);
    }
    
    @Test
    @DisplayName("즐겨찾기 추가 성공 - 201")
    void addFavorite_Success() throws Exception {
        // 새로운 가게 생성
        Store newStore = Store.builder()
                .name("새로운 맛집")
                .categoryId(koreanCategory.getCategoryId())
                .address("서울특별시 관악구 남현동 789")
                .lotNumberAddress("서울특별시 관악구 남현동 789-12")
                .latitude(new BigDecimal("37.4700"))
                .longitude(new BigDecimal("126.9400"))
                .phoneNumber("02-9999-8888")
                .averagePrice(10000)
                .reviewCount(50)
                .viewCount(300)
                .favoriteCount(20)
                .storeType(StoreType.RESTAURANT)
                .registeredAt(java.time.LocalDateTime.now())
                .build();
        newStore = storeRepository.save(newStore);
        
        AddFavoriteRequest request = AddFavoriteRequest.builder()
                .storeId(newStore.getStoreId())
                .build();
        
        mockMvc.perform(post("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.favoriteId").exists())
                .andExpect(jsonPath("$.data.storeId").value(newStore.getStoreId()))
                .andExpect(jsonPath("$.data.priority").value(3))
                .andDo(document("favorite-add-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("즐겨찾기할 가게 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.favoriteId").type(JsonFieldType.NUMBER).description("생성된 즐겨찾기 ID"),
                                fieldWithPath("data.storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("data.priority").type(JsonFieldType.NUMBER).description("표시 순서 (자동 증가)"),
                                fieldWithPath("data.favoritedAt").type(JsonFieldType.STRING).description("즐겨찾기 등록 시각"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("즐겨찾기 추가 실패 - 이미 존재하는 즐겨찾기 (409)")
    void addFavorite_AlreadyExists() throws Exception {
        AddFavoriteRequest request = AddFavoriteRequest.builder()
                .storeId(koreanStore.getStoreId())
                .build();
        
        mockMvc.perform(post("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E409"))
                .andExpect(jsonPath("$.error.message").value("이미 즐겨찾기에 추가된 가게입니다."))
                .andDo(document("favorite-add-conflict",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("즐겨찾기할 가게 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E409)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 상세 정보").optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("즐겨찾기 추가 실패 - 존재하지 않는 가게 (404)")
    void addFavorite_StoreNotFound() throws Exception {
        AddFavoriteRequest request = AddFavoriteRequest.builder()
                .storeId(99999L)
                .build();
        
        mockMvc.perform(post("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 가게입니다."))
                .andDo(document("favorite-add-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("즐겨찾기할 가게 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E404)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 상세 정보").optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("즐겨찾기 목록 조회 성공 - 200")
    void getFavorites_Success() throws Exception {
        mockMvc.perform(get("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.favorites").isArray())
                .andExpect(jsonPath("$.data.favorites.length()").value(2))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andDo(document("favorite-get-list-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.favorites").type(JsonFieldType.ARRAY).description("즐겨찾기 목록"),
                                fieldWithPath("data.favorites[].favoriteId").type(JsonFieldType.NUMBER).description("즐겨찾기 ID"),
                                fieldWithPath("data.favorites[].storeId").type(JsonFieldType.NUMBER).description("가게 ID"),
                                fieldWithPath("data.favorites[].storeName").type(JsonFieldType.STRING).description("가게명"),
                                fieldWithPath("data.favorites[].categoryName").type(JsonFieldType.STRING).description("카테고리명"),
                                fieldWithPath("data.favorites[].reviewCount").type(JsonFieldType.NUMBER).description("리뷰 수"),
                                fieldWithPath("data.favorites[].averagePrice").type(JsonFieldType.NUMBER).description("평균 가격"),
                                fieldWithPath("data.favorites[].address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.favorites[].imageUrl").type(JsonFieldType.STRING).description("대표 이미지 URL").optional(),
                                fieldWithPath("data.favorites[].priority").type(JsonFieldType.NUMBER).description("표시 순서"),
                                fieldWithPath("data.favorites[].favoritedAt").type(JsonFieldType.STRING).description("즐겨찾기 등록 시각"),
                                fieldWithPath("data.totalCount").type(JsonFieldType.NUMBER).description("전체 즐겨찾기 개수"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("즐겨찾기 목록 조회 - 빈 목록 (200)")
    void getFavorites_Empty() throws Exception {
        // 모든 즐겨찾기 삭제
        favoriteRepository.delete(favorite1);
        favoriteRepository.delete(favorite2);
        
        mockMvc.perform(get("/api/v1/favorites")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.favorites").isArray())
                .andExpect(jsonPath("$.data.favorites").isEmpty())
                .andExpect(jsonPath("$.data.totalCount").value(0))
                .andDo(document("favorite-get-list-empty",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.favorites").type(JsonFieldType.ARRAY).description("즐겨찾기 목록 (빈 배열)"),
                                fieldWithPath("data.totalCount").type(JsonFieldType.NUMBER).description("전체 즐겨찾기 개수 (0)"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("즐겨찾기 순서 변경 성공 - 200")
    void reorderFavorites_Success() throws Exception {
        // 순서 변경: favorite1 (priority 1 -> 2), favorite2 (priority 2 -> 1)
        List<ReorderFavoritesRequest.FavoriteOrderDto> orderList = Arrays.asList(
                ReorderFavoritesRequest.FavoriteOrderDto.builder()
                        .favoriteId(favorite1.getFavoriteId())
                        .priority(2L)
                        .build(),
                ReorderFavoritesRequest.FavoriteOrderDto.builder()
                        .favoriteId(favorite2.getFavoriteId())
                        .priority(1L)
                        .build()
        );
        
        ReorderFavoritesRequest request = ReorderFavoritesRequest.builder()
                .favoriteOrders(orderList)
                .build();
        
        mockMvc.perform(put("/api/v1/favorites/reorder")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.updatedCount").value(2))
                .andExpect(jsonPath("$.data.message").value("즐겨찾기 순서가 성공적으로 변경되었습니다."))
                .andDo(document("favorite-reorder-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("favoriteOrders").type(JsonFieldType.ARRAY).description("순서 변경 정보 목록"),
                                fieldWithPath("favoriteOrders[].favoriteId").type(JsonFieldType.NUMBER).description("즐겨찾기 ID"),
                                fieldWithPath("favoriteOrders[].priority").type(JsonFieldType.NUMBER).description("새로운 우선순위 값")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.updatedCount").type(JsonFieldType.NUMBER).description("변경된 즐겨찾기 개수"),
                                fieldWithPath("data.message").type(JsonFieldType.STRING).description("성공 메시지"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("즐겨찾기 순서 변경 실패 - 다른 사용자의 즐겨찾기 (403)")
    void reorderFavorites_Forbidden() throws Exception {
        // 다른 사용자 생성
        Member otherMember = Member.create(member.getGroupId(), "다른사용자", RecommendationType.SAVER);
        otherMember = memberRepository.save(otherMember);
        
        MemberAuthentication otherAuth = MemberAuthentication.createEmailAuth(
                otherMember.getMemberId(),
                "other@example.com",
                "hashedPassword",
                "다른사용자"
        );
        memberAuthenticationRepository.save(otherAuth);
        
        String otherAccessToken = createAccessToken(otherMember.getMemberId());
        
        // 다른 사용자의 토큰으로 기존 사용자의 즐겨찾기 순서 변경 시도
        List<ReorderFavoritesRequest.FavoriteOrderDto> orderList = Arrays.asList(
                ReorderFavoritesRequest.FavoriteOrderDto.builder()
                        .favoriteId(favorite1.getFavoriteId())
                        .priority(2L)
                        .build()
        );
        
        ReorderFavoritesRequest request = ReorderFavoritesRequest.builder()
                .favoriteOrders(orderList)
                .build();
        
        mockMvc.perform(put("/api/v1/favorites/reorder")
                        .header("Authorization", otherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E403"))
                .andExpect(jsonPath("$.error.message").value("다른 사용자의 리소스에 접근할 수 없습니다."))
                .andDo(document("favorite-reorder-forbidden",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("favoriteOrders").type(JsonFieldType.ARRAY).description("순서 변경 정보 목록"),
                                fieldWithPath("favoriteOrders[].favoriteId").type(JsonFieldType.NUMBER).description("즐겨찾기 ID"),
                                fieldWithPath("favoriteOrders[].priority").type(JsonFieldType.NUMBER).description("새로운 우선순위 값")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E403)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 상세 정보").optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("즐겨찾기 삭제 성공 - 200")
    void deleteFavorite_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/favorites/{favoriteId}", favorite1.getFavoriteId())
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.favoriteId").value(favorite1.getFavoriteId()))
                .andExpect(jsonPath("$.data.message").value("즐겨찾기가 성공적으로 삭제되었습니다."))
                .andDo(document("favorite-delete-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("favoriteId").description("삭제할 즐겨찾기 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.favoriteId").type(JsonFieldType.NUMBER).description("삭제된 즐겨찾기 ID"),
                                fieldWithPath("data.message").type(JsonFieldType.STRING).description("성공 메시지"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("즐겨찾기 삭제 실패 - 존재하지 않는 즐겨찾기 (404)")
    void deleteFavorite_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/favorites/{favoriteId}", 99999L)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 즐겨찾기입니다."))
                .andDo(document("favorite-delete-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("favoriteId").description("삭제할 즐겨찾기 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E404)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 상세 정보").optional()
                        )
                ));
    }
    
    @Test
    @DisplayName("즐겨찾기 삭제 실패 - 다른 사용자의 즐겨찾기 (403)")
    void deleteFavorite_Forbidden() throws Exception {
        // 다른 사용자 생성
        Member otherMember = Member.create(member.getGroupId(), "다른사용자", RecommendationType.SAVER);
        otherMember = memberRepository.save(otherMember);
        
        MemberAuthentication otherAuth = MemberAuthentication.createEmailAuth(
                otherMember.getMemberId(),
                "other@example.com",
                "hashedPassword",
                "다른사용자"
        );
        memberAuthenticationRepository.save(otherAuth);
        
        String otherAccessToken = createAccessToken(otherMember.getMemberId());
        
        mockMvc.perform(delete("/api/v1/favorites/{favoriteId}", favorite1.getFavoriteId())
                        .header("Authorization", otherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E403"))
                .andExpect(jsonPath("$.error.message").value("다른 사용자의 리소스에 접근할 수 없습니다."))
                .andDo(document("favorite-delete-forbidden",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("favoriteId").description("삭제할 즐겨찾기 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E403)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 상세 정보").optional()
                        )
                ));
    }
}
