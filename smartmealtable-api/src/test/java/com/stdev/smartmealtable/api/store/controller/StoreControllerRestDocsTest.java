package com.stdev.smartmealtable.api.store.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.domain.common.vo.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 가게 API REST Docs 테스트
 */
@DisplayName("StoreController REST Docs 테스트")
@Transactional
class StoreControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository authenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AddressHistoryRepository addressHistoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Member member;
    private String accessToken;
    private Store store1;
    private Store store2;
    private Long testCategoryId = 1L;  // 테스트용 카테고리 ID

    @BeforeEach
    void setUp() {
        // 테스트 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시");
        Group savedGroup = groupRepository.save(testGroup);

        // 테스트 회원 생성
        member = Member.create(savedGroup.getGroupId(), "가게테스트회원", null, RecommendationType.BALANCED);
        member = memberRepository.save(member);

        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(), "store@example.com", "hashedPassword", "가게테스트회원"
        );
        authenticationRepository.save(auth);

        // JWT 토큰 생성
        accessToken = jwtTokenProvider.createToken(member.getMemberId());

        // 테스트 주소 생성 (기본 주소)
        Address address = Address.of(
                "우리집",  // alias
                "서울특별시 강남구 역삼동 123-45",  // lotNumberAddress
                "서울특별시 강남구 테헤란로 123",    // streetNameAddress
                "",  // detailedAddress
                37.5012345,  // latitude
                127.0398765,  // longitude
                "HOME"  // addressType
        );
        AddressHistory addressHistory = AddressHistory.create(
                member.getMemberId(),
                address,
                true  // isPrimary
        );
        addressHistoryRepository.save(addressHistory);

        // 테스트 가게 1 생성 (가까운 거리)
        store1 = Store.builder()
                .name("맛있는 한식당")
                .categoryId(testCategoryId)
                .sellerId(1L)
                .address("서울특별시 강남구 테헤란로 100")
                .lotNumberAddress("서울특별시 강남구 역삼동 100-10")
                .latitude(new BigDecimal("37.5015678"))
                .longitude(new BigDecimal("127.0395432"))
                .phoneNumber("02-1234-5678")
                .description("신선한 재료로 만드는 한식 전문점")
                .averagePrice(8000)
                .reviewCount(150)
                .viewCount(500)
                .favoriteCount(30)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store1.jpg")
                .registeredAt(LocalDateTime.now().minusMonths(3))
                .deletedAt(null)
                .build();
        store1 = storeRepository.save(store1);

        // 테스트 가게 2 생성 (조금 먼 거리)
        store2 = Store.builder()
                .name("학생식당")
                .categoryId(testCategoryId)
                .sellerId(2L)
                .address("서울특별시 강남구 테헤란로 200")
                .lotNumberAddress("서울특별시 강남구 역삼동 200-20")
                .latitude(new BigDecimal("37.5025678"))
                .longitude(new BigDecimal("127.0405432"))
                .phoneNumber("02-2345-6789")
                .description("저렴한 학생 식당")
                .averagePrice(5000)
                .reviewCount(80)
                .viewCount(300)
                .favoriteCount(15)
                .storeType(StoreType.CAMPUS_RESTAURANT)
                .imageUrl("https://example.com/store2.jpg")
                .registeredAt(LocalDateTime.now().minusMonths(1))
                .deletedAt(null)
                .build();
        store2 = storeRepository.save(store2);
    }

    @Test
    @DisplayName("가게 목록 조회 성공 - 기본 조회")
    void getStores_success_basic_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andDo(document("store/get-list-basic",
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
                                        .description("응답 데이터"),
                                fieldWithPath("data.stores")
                                        .type(JsonFieldType.ARRAY)
                                        .description("가게 목록"),
                                fieldWithPath("data.stores[].storeId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 ID"),
                                fieldWithPath("data.stores[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("가게명"),
                                fieldWithPath("data.stores[].categoryId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.stores[].categoryName")
                                        .type(JsonFieldType.STRING)
                                        .description("카테고리명")
                                        .optional(),
                                fieldWithPath("data.stores[].address")
                                        .type(JsonFieldType.STRING)
                                        .description("도로명 주소"),
                                fieldWithPath("data.stores[].latitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("위도"),
                                fieldWithPath("data.stores[].longitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("경도"),
                                fieldWithPath("data.stores[].phoneNumber")
                                        .type(JsonFieldType.STRING)
                                        .description("전화번호"),
                                fieldWithPath("data.stores[].averagePrice")
                                        .type(JsonFieldType.NUMBER)
                                        .description("평균 가격"),
                                fieldWithPath("data.stores[].reviewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("리뷰 수"),
                                fieldWithPath("data.stores[].viewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("조회수"),
                                fieldWithPath("data.stores[].storeType")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 유형 (CAMPUS_RESTAURANT, GENERAL_RESTAURANT)"),
                                fieldWithPath("data.stores[].imageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("대표 이미지 URL")
                                        .optional(),
                                fieldWithPath("data.stores[].distance")
                                        .type(JsonFieldType.NUMBER)
                                        .description("거리 (km)")
                                        .optional(),
                                fieldWithPath("data.totalCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("전체 가게 수"),
                                fieldWithPath("data.totalPages")
                                        .type(JsonFieldType.NUMBER)
                                        .description("전체 페이지 수"),
                                fieldWithPath("data.currentPage")
                                        .type(JsonFieldType.NUMBER)
                                        .description("현재 페이지 번호"),
                                fieldWithPath("data.pageSize")
                                        .type(JsonFieldType.NUMBER)
                                        .description("페이지 크기"),
                                fieldWithPath("data.hasMore")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("다음 데이터 존재 여부"),
                                fieldWithPath("data.lastId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("마지막 가게 ID (커서 페이징용)")
                                        .optional(),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("가게 목록 조회 성공 - 필터 및 정렬 옵션 포함")
    void getStores_success_withFilters_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "한식")
                        .param("radius", "5.0")
                        .param("categoryId", testCategoryId.toString())
                        .param("storeType", "RESTAURANT")
                        .param("sortBy", "distance")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("store/get-list-with-filters",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        queryParameters(
                                parameterWithName("keyword")
                                        .description("검색어 (가게명, 카테고리명)")
                                        .optional(),
                                parameterWithName("radius")
                                        .description("검색 반경 (km, 0-50, 기본값: 3.0)")
                                        .optional(),
                                parameterWithName("categoryId")
                                        .description("카테고리 ID 필터")
                                        .optional(),
                                parameterWithName("storeType")
                                        .description("가게 유형 필터 (CAMPUS_RESTAURANT, GENERAL_RESTAURANT)")
                                        .optional(),
                                parameterWithName("sortBy")
                                        .description("정렬 기준 (distance, reviewCount, viewCount, averagePrice)")
                                        .optional(),
                                parameterWithName("page")
                                        .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                        .optional(),
                                parameterWithName("size")
                                        .description("페이지 크기 (1-100, 기본값: 20)")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.stores")
                                        .type(JsonFieldType.ARRAY)
                                        .description("가게 목록"),
                                fieldWithPath("data.stores[].storeId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 ID"),
                                fieldWithPath("data.stores[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("가게명"),
                                fieldWithPath("data.stores[].categoryId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.stores[].categoryName")
                                        .type(JsonFieldType.STRING)
                                        .description("카테고리명")
                                        .optional(),
                                fieldWithPath("data.stores[].address")
                                        .type(JsonFieldType.STRING)
                                        .description("도로명 주소"),
                                fieldWithPath("data.stores[].latitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("위도"),
                                fieldWithPath("data.stores[].longitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("경도"),
                                fieldWithPath("data.stores[].phoneNumber")
                                        .type(JsonFieldType.STRING)
                                        .description("전화번호"),
                                fieldWithPath("data.stores[].averagePrice")
                                        .type(JsonFieldType.NUMBER)
                                        .description("평균 가격"),
                                fieldWithPath("data.stores[].reviewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("리뷰 수"),
                                fieldWithPath("data.stores[].viewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("조회수"),
                                fieldWithPath("data.stores[].storeType")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 유형"),
                                fieldWithPath("data.stores[].imageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("대표 이미지 URL")
                                        .optional(),
                                fieldWithPath("data.stores[].distance")
                                        .type(JsonFieldType.NUMBER)
                                        .description("거리 (km)")
                                        .optional(),
                                fieldWithPath("data.totalCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("전체 가게 수"),
                                fieldWithPath("data.totalPages")
                                        .type(JsonFieldType.NUMBER)
                                        .description("전체 페이지 수"),
                                fieldWithPath("data.currentPage")
                                        .type(JsonFieldType.NUMBER)
                                        .description("현재 페이지 번호"),
                                fieldWithPath("data.pageSize")
                                        .type(JsonFieldType.NUMBER)
                                        .description("페이지 크기"),
                                fieldWithPath("data.hasMore")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("다음 데이터 존재 여부"),
                                fieldWithPath("data.lastId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("마지막 가게 ID (커서 페이징용)")
                                        .optional(),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("가게 상세 조회")
    void getStores_fail_invalidRadius_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("radius", "100"))  // 최대 50km
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E400"))
                .andDo(document("store/get-list-invalid-radius",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        queryParameters(
                                parameterWithName("radius")
                                        .description("검색 반경 (유효하지 않은 값)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .description("응답 데이터 (에러 시 null)")
                                        .optional(),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E400)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 상세 정보")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("가게 상세 조회 성공")
    void getStoreDetail_success_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}", store1.getStoreId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeId").value(store1.getStoreId()))
                .andDo(document("store/get-detail-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        pathParameters(
                                parameterWithName("storeId")
                                        .description("조회할 가게 ID")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.storeId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 ID"),
                                fieldWithPath("data.name")
                                        .type(JsonFieldType.STRING)
                                        .description("가게명"),
                                fieldWithPath("data.categoryId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.categoryName")
                                        .type(JsonFieldType.STRING)
                                        .description("카테고리명")
                                        .optional(),
                                fieldWithPath("data.address")
                                        .type(JsonFieldType.STRING)
                                        .description("도로명 주소"),
                                fieldWithPath("data.lotNumberAddress")
                                        .type(JsonFieldType.STRING)
                                        .description("지번 주소"),
                                fieldWithPath("data.latitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("위도"),
                                fieldWithPath("data.longitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("경도"),
                                fieldWithPath("data.phoneNumber")
                                        .type(JsonFieldType.STRING)
                                        .description("전화번호"),
                                fieldWithPath("data.description")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 설명"),
                                fieldWithPath("data.averagePrice")
                                        .type(JsonFieldType.NUMBER)
                                        .description("평균 가격"),
                                fieldWithPath("data.reviewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("리뷰 수"),
                                fieldWithPath("data.viewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("조회수"),
                                fieldWithPath("data.favoriteCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("즐겨찾기 수"),
                                fieldWithPath("data.storeType")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 유형 (CAMPUS_RESTAURANT, GENERAL_RESTAURANT)"),
                                fieldWithPath("data.imageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("대표 이미지 URL")
                                        .optional(),
                                fieldWithPath("data.openingHours")
                                        .type(JsonFieldType.ARRAY)
                                        .description("영업시간 정보")
                                        .optional(),
                                fieldWithPath("data.openingHours[].dayOfWeek")
                                        .type(JsonFieldType.STRING)
                                        .description("요일 (MONDAY-SUNDAY)")
                                        .optional(),
                                fieldWithPath("data.openingHours[].openTime")
                                        .type(JsonFieldType.STRING)
                                        .description("영업 시작 시간 (HH:mm)")
                                        .optional(),
                                fieldWithPath("data.openingHours[].closeTime")
                                        .type(JsonFieldType.STRING)
                                        .description("영업 종료 시간 (HH:mm)")
                                        .optional(),
                                fieldWithPath("data.openingHours[].breakStartTime")
                                        .type(JsonFieldType.STRING)
                                        .description("휴게 시작 시간 (HH:mm)")
                                        .optional(),
                                fieldWithPath("data.openingHours[].breakEndTime")
                                        .type(JsonFieldType.STRING)
                                        .description("휴게 종료 시간 (HH:mm)")
                                        .optional(),
                                fieldWithPath("data.openingHours[].isHoliday")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("휴무일 여부")
                                        .optional(),
                                fieldWithPath("data.temporaryClosures")
                                        .type(JsonFieldType.ARRAY)
                                        .description("임시 휴무 정보")
                                        .optional(),
                                fieldWithPath("data.temporaryClosures[].closureDate")
                                        .type(JsonFieldType.STRING)
                                        .description("휴무 날짜 (yyyy-MM-dd)")
                                        .optional(),
                                fieldWithPath("data.temporaryClosures[].startTime")
                                        .type(JsonFieldType.STRING)
                                        .description("휴무 시작 시간 (HH:mm:ss)")
                                        .optional(),
                                fieldWithPath("data.temporaryClosures[].endTime")
                                        .type(JsonFieldType.STRING)
                                        .description("휴무 종료 시간 (HH:mm:ss)")
                                        .optional(),
                                fieldWithPath("data.temporaryClosures[].reason")
                                        .type(JsonFieldType.STRING)
                                        .description("휴무 사유")
                                        .optional(),
                                fieldWithPath("data.menus")
                                        .type(JsonFieldType.ARRAY)
                                        .description("음식/메뉴 목록")
                                        .optional(),
                                fieldWithPath("data.menus[].foodId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("음식 ID")
                                        .optional(),
                                fieldWithPath("data.menus[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("음식명")
                                        .optional(),
                                fieldWithPath("data.menus[].storeId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 ID")
                                        .optional(),
                                fieldWithPath("data.menus[].categoryId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("음식 카테고리 ID")
                                        .optional(),
                                fieldWithPath("data.menus[].description")
                                        .type(JsonFieldType.STRING)
                                        .description("음식 설명")
                                        .optional(),
                                fieldWithPath("data.menus[].imageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("음식 이미지 URL")
                                        .optional(),
                                fieldWithPath("data.menus[].averagePrice")
                                        .type(JsonFieldType.NUMBER)
                                        .description("평균 가격")
                                        .optional(),
                                fieldWithPath("data.isFavorite")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("즐겨찾기 여부"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("가게 상세 조회 실패 - 존재하지 않는 가게")
    void getStoreDetail_fail_notFound_docs() throws Exception {
        // given
        Long nonExistentStoreId = 99999L;

        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}", nonExistentStoreId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andDo(document("store/get-detail-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        pathParameters(
                                parameterWithName("storeId")
                                        .description("존재하지 않는 가게 ID")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .description("응답 데이터 (에러 시 null)")
                                        .optional(),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E404)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 상세 정보")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("가게 자동완성 검색 성공")
    void autocomplete_success_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/autocomplete")
                        .param("keyword", "한식")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andDo(document("store/autocomplete-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword")
                                        .description("검색 키워드"),
                                parameterWithName("limit")
                                        .description("조회 개수 (1-20, 기본값: 10)")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.stores")
                                        .type(JsonFieldType.ARRAY)
                                        .description("자동완성 검색 결과"),
                                fieldWithPath("data.stores[].storeId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 ID"),
                                fieldWithPath("data.stores[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("가게명"),
                                fieldWithPath("data.stores[].categoryName")
                                        .type(JsonFieldType.STRING)
                                        .description("카테고리명")
                                        .optional(),
                                fieldWithPath("data.stores[].address")
                                        .type(JsonFieldType.STRING)
                                        .description("도로명 주소"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("가게 자동완성 검색 실패 - 유효하지 않은 limit")
    void autocomplete_fail_invalidLimit_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/autocomplete")
                        .param("keyword", "한식")
                        .param("limit", "100"))  // 최대 20
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E400"))
                .andDo(document("store/autocomplete-invalid-limit",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword")
                                        .description("검색 키워드"),
                                parameterWithName("limit")
                                        .description("조회 개수 (유효하지 않은 값)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .description("응답 데이터 (에러 시 null)")
                                        .optional(),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E400)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 상세 정보")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("가게 목록 조회 성공 - 커서 기반 무한 스크롤 (첫 요청)")
    void getStores_success_cursorPagination_first_docs() throws Exception {
        // when & then: 첫 요청 (lastId 없음)
        mockMvc.perform(get("/api/v1/stores")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "한식")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("store/get-list-cursor-first",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        queryParameters(
                                parameterWithName("keyword")
                                        .description("검색어 (가게명, 카테고리명)")
                                        .optional(),
                                parameterWithName("limit")
                                        .description("커서 모드 조회 개수 (1-100, 기본값: 20)")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.stores")
                                        .type(JsonFieldType.ARRAY)
                                        .description("가게 목록"),
                                fieldWithPath("data.stores[].storeId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 ID"),
                                fieldWithPath("data.stores[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("가게명"),
                                fieldWithPath("data.stores[].categoryId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.stores[].categoryName")
                                        .type(JsonFieldType.NULL)
                                        .description("카테고리명 (현재 NULL, 추후 카테고리 조인 시 추가 예정)")
                                        .optional(),
                                fieldWithPath("data.stores[].address")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 주소"),
                                fieldWithPath("data.stores[].latitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 위도"),
                                fieldWithPath("data.stores[].longitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 경도"),
                                fieldWithPath("data.stores[].averagePrice")
                                        .type(JsonFieldType.NUMBER)
                                        .description("평균 가격"),
                                fieldWithPath("data.stores[].reviewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("리뷰 수"),
                                fieldWithPath("data.stores[].viewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("조회 수"),
                                fieldWithPath("data.stores[].storeType")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 유형 (예: RESTAURANT, CAFE)"),
                                fieldWithPath("data.stores[].imageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 이미지 URL"),
                                fieldWithPath("data.stores[].distance")
                                        .type(JsonFieldType.NUMBER)
                                        .description("거리 (km)"),
                                fieldWithPath("data.stores[].phoneNumber")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 전화번호"),
                                fieldWithPath("data.totalCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("전체 가게 수"),
                                fieldWithPath("data.hasMore")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("다음 데이터 존재 여부"),
                                fieldWithPath("data.lastId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("마지막 가게 ID (다음 요청의 cursor)"),
                                fieldWithPath("data.currentPage")
                                        .type(JsonFieldType.NUMBER)
                                        .description("현재 페이지 (커서 모드에서는 0)"),
                                fieldWithPath("data.pageSize")
                                        .type(JsonFieldType.NUMBER)
                                        .description("페이지 크기"),
                                fieldWithPath("data.totalPages")
                                        .type(JsonFieldType.NUMBER)
                                        .description("전체 페이지 수 (커서 모드에서는 1)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("가게 목록 조회 성공 - 커서 기반 무한 스크롤 (다음 요청)")
    void getStores_success_cursorPagination_next_docs() throws Exception {
        // when & then: 다음 요청 (lastId = 첫 번째 가게의 storeId)
        mockMvc.perform(get("/api/v1/stores")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "한식")
                        .param("lastId", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("store/get-list-cursor-next",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        queryParameters(
                                parameterWithName("keyword")
                                        .description("검색어 (가게명, 카테고리명)")
                                        .optional(),
                                parameterWithName("lastId")
                                        .description("커서 ID (이전 응답의 lastId)")
                                        .optional(),
                                parameterWithName("limit")
                                        .description("커서 모드 조회 개수 (1-100, 기본값: 20)")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.stores")
                                        .type(JsonFieldType.ARRAY)
                                        .description("가게 목록"),
                                fieldWithPath("data.stores[].storeId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 ID"),
                                fieldWithPath("data.stores[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("가게명"),
                                fieldWithPath("data.stores[].categoryId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.stores[].categoryName")
                                        .type(JsonFieldType.NULL)
                                        .description("카테고리명 (현재 NULL, 추후 카테고리 조인 시 추가 예정)")
                                        .optional(),
                                fieldWithPath("data.stores[].address")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 주소"),
                                fieldWithPath("data.stores[].latitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 위도"),
                                fieldWithPath("data.stores[].longitude")
                                        .type(JsonFieldType.NUMBER)
                                        .description("가게 경도"),
                                fieldWithPath("data.stores[].averagePrice")
                                        .type(JsonFieldType.NUMBER)
                                        .description("평균 가격"),
                                fieldWithPath("data.stores[].reviewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("리뷰 수"),
                                fieldWithPath("data.stores[].viewCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("조회 수"),
                                fieldWithPath("data.stores[].storeType")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 유형 (예: RESTAURANT, CAFE)"),
                                fieldWithPath("data.stores[].imageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 이미지 URL"),
                                fieldWithPath("data.stores[].distance")
                                        .type(JsonFieldType.NUMBER)
                                        .description("거리 (km)"),
                                fieldWithPath("data.stores[].phoneNumber")
                                        .type(JsonFieldType.STRING)
                                        .description("가게 전화번호"),
                                fieldWithPath("data.totalCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("전체 가게 수"),
                                fieldWithPath("data.hasMore")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("다음 데이터 존재 여부"),
                                fieldWithPath("data.lastId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("마지막 가게 ID (다음 요청의 cursor)"),
                                fieldWithPath("data.currentPage")
                                        .type(JsonFieldType.NUMBER)
                                        .description("현재 페이지 (커서 모드에서는 0)"),
                                fieldWithPath("data.pageSize")
                                        .type(JsonFieldType.NUMBER)
                                        .description("페이지 크기"),
                                fieldWithPath("data.totalPages")
                                        .type(JsonFieldType.NUMBER)
                                        .description("전체 페이지 수 (커서 모드에서는 1)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }
}
