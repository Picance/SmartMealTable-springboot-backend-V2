package com.stdev.smartmealtable.api.store.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.store.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 가게 상세 조회 API 통합 테스트
 * TDD: RED-GREEN-REFACTOR
 */
@DisplayName("가게 상세 조회 API 테스트")
class GetStoreDetailControllerTest extends AbstractRestDocsTest {
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private StoreOpeningHourRepository storeOpeningHourRepository;
    
    @Autowired
    private StoreTemporaryClosureRepository storeTemporaryClosureRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;
    
    @Autowired
    private AddressHistoryRepository addressHistoryRepository;
    
    private Member testMember;
    private Store testStore;
    private String jwtToken;
    
    @BeforeEach
    void setUp() {
        // 테스트 회원 생성 및 저장
        testMember = Member.create(null, "테스트유저", null, RecommendationType.BALANCED);
        testMember = memberRepository.save(testMember); // 저장된 객체 다시 할당하여 ID 확보
        
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                testMember.getMemberId(),
                "test@example.com",
                "hashedPassword123",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);
        
        // 기본 주소 생성
        Address addressValue = Address.of(
                "집",
                "서울특별시 강남구 역삼동 825",
                "서울특별시 강남구 강남대로 396",
                "101동 101호",
                37.497952,
                127.027619,
                AddressType.HOME
        );
        AddressHistory testAddress = AddressHistory.create(
                testMember.getMemberId(),
                addressValue,
                true
        );
        addressHistoryRepository.save(testAddress);
        
        // JWT 토큰 생성 (AbstractRestDocsTest의 createAccessToken 사용)
        jwtToken = createAccessToken(testMember.getMemberId());
        
        // 테스트 가게 데이터 생성
        testStore = Store.builder()
                .name("강남 한정식")
                .categoryIds(java.util.List.of(1L))
                .sellerId(1L)
                .address("서울특별시 강남구 강남대로 400")
                .lotNumberAddress("서울특별시 강남구 역삼동 826")
                .latitude(new BigDecimal("37.498500"))
                .longitude(new BigDecimal("127.028000"))
                .phoneNumber("02-1234-5678")
                .description("정통 한정식을 제공합니다")
                .averagePrice(15000)
                .reviewCount(120)
                .viewCount(1500)
                .favoriteCount(50)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store1.jpg")
                .registeredAt(LocalDateTime.now())
                .build();
        testStore = storeRepository.save(testStore);
        
        // 영업시간 데이터 생성
        createOpeningHours();
        
        // 임시 휴무 데이터 생성
        createTemporaryClosures();
    }
    
    private void createOpeningHours() {
        // 월요일 - 금요일
        for (int i = 1; i <= 5; i++) {
            StoreOpeningHour openingHour = new StoreOpeningHour(
                    null,
                    testStore.getStoreId(),
                    DayOfWeek.of(i),
                    "09:00",
                    "22:00",
                    "15:00",
                    "17:00",
                    false
            );
            storeOpeningHourRepository.save(openingHour);
        }
        
        // 토요일
        StoreOpeningHour saturday = new StoreOpeningHour(
                null,
                testStore.getStoreId(),
                DayOfWeek.SATURDAY,
                "10:00",
                "20:00",
                null,
                null,
                false
        );
        storeOpeningHourRepository.save(saturday);
        
        // 일요일 (휴무)
        StoreOpeningHour sunday = new StoreOpeningHour(
                null,
                testStore.getStoreId(),
                DayOfWeek.SUNDAY,
                null,
                null,
                null,
                null,
                true
        );
        storeOpeningHourRepository.save(sunday);
    }
    
    private void createTemporaryClosures() {
        // 오늘부터 3일 후 임시 휴무
        LocalDate closureDate = LocalDate.now().plusDays(3);
        StoreTemporaryClosure closure = new StoreTemporaryClosure(
                null,
                testStore.getStoreId(),
                closureDate,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                "단체 행사로 인한 임시 휴무"
        );
        storeTemporaryClosureRepository.save(closure);
    }
    
    @Test
    @DisplayName("가게 상세 조회 성공")
    void getStoreDetail_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}", testStore.getStoreId())
                        .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeId").value(testStore.getStoreId()))
                .andExpect(jsonPath("$.data.name").value("강남 한정식"))
                .andExpect(jsonPath("$.data.address").value("서울특별시 강남구 강남대로 400"))
                .andExpect(jsonPath("$.data.phoneNumber").value("02-1234-5678"))
                .andExpect(jsonPath("$.data.description").value("정통 한정식을 제공합니다"))
                .andExpect(jsonPath("$.data.averagePrice").value(15000))
                .andExpect(jsonPath("$.data.reviewCount").value(120))
                .andExpect(jsonPath("$.data.viewCount").value(1501)) // 조회수 증가
                .andExpect(jsonPath("$.data.openingHours").isArray())
                .andExpect(jsonPath("$.data.openingHours.length()").value(7))
                .andExpect(jsonPath("$.data.temporaryClosures").isArray())
                .andExpect(jsonPath("$.data.temporaryClosures.length()").value(1))
                .andDo(document("store-detail-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("storeId").description("가게 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("result").description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data.storeId").description("가게 ID"),
                                fieldWithPath("data.name").description("가게명"),
                                fieldWithPath("data.categoryName").description("카테고리명").optional(),
                                fieldWithPath("data.address").description("주소"),
                                fieldWithPath("data.lotNumberAddress").description("지번 주소"),
                                fieldWithPath("data.latitude").description("위도"),
                                fieldWithPath("data.longitude").description("경도"),
                                fieldWithPath("data.phoneNumber").description("전화번호"),
                                fieldWithPath("data.description").description("가게 설명"),
                                fieldWithPath("data.averagePrice").description("평균 가격"),
                                fieldWithPath("data.reviewCount").description("리뷰 수"),
                                fieldWithPath("data.viewCount").description("조회 수 (조회 시마다 증가)"),
                                fieldWithPath("data.favoriteCount").description("즐겨찾기 수"),
                                fieldWithPath("data.storeType").description("가게 유형 (RESTAURANT, CAMPUS_RESTAURANT 등)"),
                                fieldWithPath("data.imageUrl").description("가게 대표 이미지 URL"),
                                fieldWithPath("data.isFavorite").description("사용자가 즐겨찾기한 가게 여부"),
                                fieldWithPath("data.openingHours[]").description("영업시간 목록 (요일별)"),
                                fieldWithPath("data.openingHours[].dayOfWeek").description("요일 (MONDAY ~ SUNDAY)"),
                                fieldWithPath("data.temporaryClosures[]").description("임시 휴무 목록")
                        )
                ));
    }
    
    @Test
    @DisplayName("가게 상세 조회 성공 - 조회수 증가 확인")
    void getStoreDetail_Success_ViewCountIncreases() throws Exception {
        // given
        int initialViewCount = testStore.getViewCount();
        
        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}", testStore.getStoreId())
                        .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(initialViewCount + 1));
    }
    
    @Test
    @DisplayName("가게 상세 조회 실패 - 존재하지 않는 가게 (404)")
    void getStoreDetail_Fail_NotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}", 99999L)
                        .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }
    
    @Test
    @DisplayName("가게 상세 조회 실패 - 인증 토큰 없음 (401)")
    void getStoreDetail_Fail_NoAuth() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}", testStore.getStoreId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
