package com.stdev.smartmealtable.api.expenditure.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.expenditure.dto.request.CreateExpenditureRequest;
import com.stdev.smartmealtable.api.expenditure.dto.request.ParseSmsRequest;
import com.stdev.smartmealtable.api.expenditure.service.ParseSmsService;
import com.stdev.smartmealtable.api.expenditure.service.dto.ParseSmsServiceResponse;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ExpenditureController REST Docs 테스트
 */
@DisplayName("ExpenditureController REST Docs")
class ExpenditureControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ExpenditureRepository expenditureRepository;
    @Autowired
    private DailyBudgetRepository dailyBudgetRepository;
    
    @MockBean
    private ParseSmsService parseSmsService;

    private Member member;
    private String accessToken;
    private Long categoryId;

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
        Category category = Category.reconstitute(null, "한식");
        Category savedCategory = categoryRepository.save(category);
        categoryId = savedCategory.getCategoryId();
    }

    @Test
    @DisplayName("지출 내역 등록 성공 - 아이템 포함")
    void createExpenditure_WithItems_Success() throws Exception {
        // given
        CreateExpenditureRequest.ExpenditureItemRequest item1 =
                new CreateExpenditureRequest.ExpenditureItemRequest(1L, 1, 8000);
        CreateExpenditureRequest.ExpenditureItemRequest item2 =
                new CreateExpenditureRequest.ExpenditureItemRequest(2L, 1, 7000);

        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "맛있는집",
                15000,
                LocalDate.of(2025, 10, 12),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                "점심 식사",
                List.of(item1, item2)
        );

        // when & then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.expenditureId").exists())
                .andExpect(jsonPath("$.data.storeName").value("맛있는집"))
                .andExpect(jsonPath("$.data.amount").value(15000))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isNotEmpty())
                .andDo(document("expenditure/create-expenditure-with-items-success",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름 (최대 200자)"),
                                fieldWithPath("amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액 (0 이상)"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜 (yyyy-MM-dd)"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간 (HH:mm:ss)").optional(),
                                fieldWithPath("categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID").optional(),
                                fieldWithPath("mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형 (BREAKFAST, LUNCH, DINNER, OTHER)").optional(),
                                fieldWithPath("memo").type(JsonFieldType.STRING)
                                        .description("메모 (최대 500자)").optional(),
                                fieldWithPath("items").type(JsonFieldType.ARRAY)
                                        .description("지출 항목 목록").optional(),
                                fieldWithPath("items[].foodId").type(JsonFieldType.NUMBER)
                                        .description("음식 ID"),
                                fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER)
                                        .description("수량 (1 이상)"),
                                fieldWithPath("items[].price").type(JsonFieldType.NUMBER)
                                        .description("가격 (0 이상)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.expenditureId").type(JsonFieldType.NUMBER)
                                        .description("생성된 지출 내역 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름"),
                                fieldWithPath("data.amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액"),
                                fieldWithPath("data.expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜"),
                                fieldWithPath("data.expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간"),
                                fieldWithPath("data.categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.categoryName").type(JsonFieldType.STRING)
                                        .description("카테고리 이름"),
                                fieldWithPath("data.mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형"),
                                fieldWithPath("data.memo").type(JsonFieldType.STRING)
                                        .description("메모"),
                                fieldWithPath("data.items").type(JsonFieldType.ARRAY)
                                        .description("지출 항목 목록"),
                                fieldWithPath("data.items[].expenditureItemId").type(JsonFieldType.NUMBER)
                                        .description("지출 항목 ID"),
                                fieldWithPath("data.items[].foodId").type(JsonFieldType.NUMBER)
                                        .description("음식 ID"),
                                fieldWithPath("data.items[].foodName").type(JsonFieldType.STRING)
                                        .description("음식 이름"),
                                fieldWithPath("data.items[].quantity").type(JsonFieldType.NUMBER)
                                        .description("수량"),
                                fieldWithPath("data.items[].price").type(JsonFieldType.NUMBER)
                                        .description("가격"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING)
                                        .description("생성 시각")
                        )
                ));
    }

    @Test
    @DisplayName("지출 내역 등록 성공 - 아이템 없이 간단 등록")
    void createExpenditure_WithoutItems_Success() throws Exception {
        // given
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "편의점",
                5000,
                LocalDate.of(2025, 10, 12),
                LocalTime.of(14, 0),
                categoryId,
                MealType.OTHER,
                null,
                null
        );

        // when & then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.expenditureId").exists())
                .andExpect(jsonPath("$.data.storeName").value("편의점"))
                .andExpect(jsonPath("$.data.amount").value(5000))
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andDo(document("expenditure/create-expenditure-without-items-success",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름"),
                                fieldWithPath("amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간"),
                                fieldWithPath("categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형"),
                                fieldWithPath("memo").type(JsonFieldType.NULL)
                                        .description("메모 (없음)"),
                                fieldWithPath("items").type(JsonFieldType.NULL)
                                        .description("지출 항목 목록 (없음)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.expenditureId").type(JsonFieldType.NUMBER)
                                        .description("생성된 지출 내역 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름"),
                                fieldWithPath("data.amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액"),
                                fieldWithPath("data.expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜"),
                                fieldWithPath("data.expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간"),
                                fieldWithPath("data.categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.categoryName").type(JsonFieldType.STRING)
                                        .description("카테고리 이름"),
                                fieldWithPath("data.mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형"),
                                fieldWithPath("data.memo").type(JsonFieldType.NULL)
                                        .description("메모 (없음)"),
                                fieldWithPath("data.items").type(JsonFieldType.ARRAY)
                                        .description("지출 항목 목록 (빈 배열)"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING)
                                        .description("생성 시각")
                        )
                ));
    }

    @Test
    @DisplayName("지출 내역 등록 실패 - 유효성 검증 실패 (필수 필드 누락)")
    void createExpenditure_ValidationFailed() throws Exception {
        // given - storeName이 null
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                null,  // storeName 누락
                15000,
                LocalDate.of(2025, 10, 12),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                null,
                null
        );

        // when & then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("expenditure/create-expenditure-validation-failed",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("storeName").type(JsonFieldType.NULL)
                                        .description("가게 이름 (필수, 누락됨)"),
                                fieldWithPath("amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간"),
                                fieldWithPath("categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형"),
                                fieldWithPath("memo").type(JsonFieldType.NULL)
                                        .description("메모"),
                                fieldWithPath("items").type(JsonFieldType.NULL)
                                        .description("지출 항목 목록")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT).optional()
                                        .description("에러 상세 정보 (선택적)"),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING).optional()
                                        .description("에러가 발생한 필드"),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING).optional()
                                        .description("에러 사유")
                        )
                ));
    }

    @Test
    @DisplayName("지출 내역 등록 실패 - 인증되지 않은 요청")
    void createExpenditure_Unauthorized() throws Exception {
        // given
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "맛있는집",
                15000,
                LocalDate.of(2025, 10, 12),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                null,
                null
        );

        // when & then - Authorization 헤더 없이 요청
        mockMvc.perform(post("/api/v1/expenditures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("expenditure/create-expenditure-unauthorized",
                        requestFields(
                                fieldWithPath("storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름"),
                                fieldWithPath("amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간"),
                                fieldWithPath("categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형"),
                                fieldWithPath("memo").type(JsonFieldType.NULL)
                                        .description("메모"),
                                fieldWithPath("items").type(JsonFieldType.NULL)
                                        .description("지출 항목 목록")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT).optional()
                                        .description("에러 상세 정보 (선택적)"),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING).optional()
                                        .description("에러가 발생한 필드"),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING).optional()
                                        .description("에러 사유")
                        )
                ));
    }

    @Test
    @DisplayName("SMS 파싱 성공 - KB국민카드")
    void parseSms_KBCard_Success() throws Exception {
        // given
        ParseSmsRequest request = new ParseSmsRequest(
                "[국민카드] 10/08 12:30 맘스터치강남점 13,500원 일시불 승인"
        );
        
        // Mock 설정
        ParseSmsServiceResponse mockResponse = new ParseSmsServiceResponse(
                "KB국민카드",
                LocalDateTime.of(2025, 10, 8, 12, 30),
                13500L,
                "맘스터치강남점"
        );
        when(parseSmsService.parseSms(any())).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/v1/expenditures/parse-sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeName").exists())
                .andExpect(jsonPath("$.data.amount").exists())
                .andExpect(jsonPath("$.data.vendor").exists())
                .andExpect(jsonPath("$.data.transactionDateTime").exists())
                .andDo(document("expenditure/parse-sms-success",
                        requestFields(
                                fieldWithPath("smsMessage").type(JsonFieldType.STRING)
                                        .description("파싱할 SMS 문자 메시지 원문 (카드 결제 승인 문자)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("파싱 결과 데이터"),
                                fieldWithPath("data.vendor").type(JsonFieldType.STRING)
                                        .description("카드사 이름 (예: KB국민카드, NH농협카드)"),
                                fieldWithPath("data.transactionDateTime").type(JsonFieldType.STRING)
                                        .description("거래 일시 (yyyy-MM-dd'T'HH:mm:ss)"),
                                fieldWithPath("data.amount").type(JsonFieldType.NUMBER)
                                        .description("결제 금액"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름")
                        )
                ));
    }

    @Test
    @DisplayName("SMS 파싱 성공 - NH농협카드")
    void parseSms_NHCard_Success() throws Exception {
        // given
        ParseSmsRequest request = new ParseSmsRequest(
                "[NH농협카드] 승인 10/08 14:20 스타벅스역삼점 5,500원"
        );
        
        // Mock 설정
        ParseSmsServiceResponse mockResponse = new ParseSmsServiceResponse(
                "NH농협카드",
                LocalDateTime.of(2025, 10, 8, 14, 20),
                5500L,
                "스타벅스역삼점"
        );
        when(parseSmsService.parseSms(any())).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/v1/expenditures/parse-sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeName").exists())
                .andExpect(jsonPath("$.data.amount").exists())
                .andExpect(jsonPath("$.data.vendor").exists())
                .andExpect(jsonPath("$.data.transactionDateTime").exists())
                .andDo(document("expenditure/parse-sms-nh-card-success",
                        requestFields(
                                fieldWithPath("smsMessage").type(JsonFieldType.STRING)
                                        .description("NH농협카드 결제 승인 문자")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("파싱 결과 데이터"),
                                fieldWithPath("data.vendor").type(JsonFieldType.STRING)
                                        .description("카드사 이름"),
                                fieldWithPath("data.transactionDateTime").type(JsonFieldType.STRING)
                                        .description("거래 일시"),
                                fieldWithPath("data.amount").type(JsonFieldType.NUMBER)
                                        .description("결제 금액"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름")
                        )
                ));
    }

    @Test
    @DisplayName("SMS 파싱 실패 - 빈 문자열")
    void parseSms_EmptyMessage_Failed() throws Exception {
        // given
        ParseSmsRequest request = new ParseSmsRequest("");
        
        // when & then - @NotBlank 검증 실패로 422 반환
        mockMvc.perform(post("/api/v1/expenditures/parse-sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())  // @NotBlank 검증 실패 -> 422
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").value("SMS 메시지는 필수입니다."))
                .andDo(document("expenditure/parse-sms-empty-message-failed",
                        requestFields(
                                fieldWithPath("smsMessage").type(JsonFieldType.STRING)
                                        .description("빈 문자열 (유효하지 않음)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E422: Validation Failed)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT)
                                        .description("에러 상세 정보"),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING)
                                        .description("에러가 발생한 필드"),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING)
                                        .description("에러 사유")
                        )
                ));
    }

    @Test
    @DisplayName("SMS 파싱 실패 - 파싱할 수 없는 형식")
    void parseSms_InvalidFormat_Failed() throws Exception {
        // given
        ParseSmsRequest request = new ParseSmsRequest(
                "이것은 카드 결제 문자가 아닙니다. 단순 텍스트입니다."
        );
        
        // Mock 설정 - 잘못된 형식의 경우 IllegalArgumentException 발생
        when(parseSmsService.parseSms(any())).thenThrow(
                new IllegalArgumentException("SMS 파싱에 실패했습니다.")
        );

        // when & then
        mockMvc.perform(post("/api/v1/expenditures/parse-sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())  // IllegalArgumentException은 400으로 매핑됨
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("E400"))  // 에러 코드 검증 추가
                .andDo(document("expenditure/parse-sms-invalid-format-failed",
                        requestFields(
                                fieldWithPath("smsMessage").type(JsonFieldType.STRING)
                                        .description("파싱할 수 없는 형식의 문자열")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E400: Bad Request)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("지출 통계 조회 성공")
    void getExpenditureStatistics_Success() throws Exception {
        // given - 테스트 데이터 생성
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 31);
        
        // 예산 데이터 생성
        for (int day = 1; day <= 5; day++) {
            LocalDate date = LocalDate.of(2025, 10, day);
            DailyBudget budget = DailyBudget.create(
                    member.getMemberId(),
                    10000,
                    date
            );
            dailyBudgetRepository.save(budget);
        }
        
        // 지출 데이터 생성
        for (int day = 1; day <= 5; day++) {
            LocalDate date = LocalDate.of(2025, 10, day);
            Expenditure expenditure = Expenditure.create(
                    member.getMemberId(),
                    "테스트가게" + day,
                    8000 + (day * 1000),
                    date,
                    LocalTime.of(12, 0),
                    categoryId,
                    MealType.LUNCH,
                    null,
                    List.of()  // 빈 리스트로 수정
            );
            expenditureRepository.save(expenditure);
        }

        // when & then
        mockMvc.perform(get("/api/v1/expenditures/statistics")
                        .header("Authorization", accessToken)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.dailyStatistics").isArray())
                .andDo(document("expenditure/get-statistics-success",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer)")
                        ),
                        queryParameters(
                                parameterWithName("startDate").description("조회 시작 날짜 (yyyy-MM-dd)"),
                                parameterWithName("endDate").description("조회 종료 날짜 (yyyy-MM-dd)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("통계 데이터"),
                                fieldWithPath("data.totalAmount").type(JsonFieldType.NUMBER)
                                        .description("기간 내 총 지출 금액"),
                                fieldWithPath("data.categoryStatistics").type(JsonFieldType.ARRAY)
                                        .description("카테고리별 통계 목록"),
                                fieldWithPath("data.categoryStatistics[].categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.categoryStatistics[].categoryName").type(JsonFieldType.STRING)
                                        .description("카테고리 이름"),
                                fieldWithPath("data.categoryStatistics[].amount").type(JsonFieldType.NUMBER)
                                        .description("카테고리별 지출 금액"),
                                fieldWithPath("data.dailyStatistics").type(JsonFieldType.ARRAY)
                                        .description("일별 통계 목록"),
                                fieldWithPath("data.dailyStatistics[].date").type(JsonFieldType.STRING)
                                        .description("날짜 (yyyy-MM-dd)"),
                                fieldWithPath("data.dailyStatistics[].amount").type(JsonFieldType.NUMBER)
                                        .description("해당 날짜의 총 지출 금액"),
                                fieldWithPath("data.mealTypeStatistics").type(JsonFieldType.ARRAY)
                                        .description("식사 유형별 통계 목록"),
                                fieldWithPath("data.mealTypeStatistics[].mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형 (BREAKFAST, LUNCH, DINNER, OTHER)"),
                                fieldWithPath("data.mealTypeStatistics[].amount").type(JsonFieldType.NUMBER)
                                        .description("식사 유형별 지출 금액")
                        )
                ));
    }

    @Test
    @DisplayName("지출 통계 조회 실패 - 인증되지 않은 요청")
    void getExpenditureStatistics_Unauthorized() throws Exception {
        // given
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 31);

        // when & then - Authorization 헤더 없이 요청
        mockMvc.perform(get("/api/v1/expenditures/statistics")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("expenditure/get-statistics-unauthorized",
                        queryParameters(
                                parameterWithName("startDate").description("조회 시작 날짜"),
                                parameterWithName("endDate").description("조회 종료 날짜")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E401: 인증 실패)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT).optional()
                                        .description("에러 상세 정보 (선택적)"),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING).optional()
                                        .description("에러가 발생한 필드"),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING).optional()
                                        .description("에러 사유")
                        )
                ));
    }

    // TODO: 날짜 범위 검증 로직이 구현되면 주석 해제
    /*
    @Test
    @DisplayName("지출 통계 조회 실패 - 잘못된 날짜 범위")
    void getExpenditureStatistics_InvalidDateRange() throws Exception {
        // given - 종료 날짜가 시작 날짜보다 이전
        LocalDate startDate = LocalDate.of(2025, 10, 31);
        LocalDate endDate = LocalDate.of(2025, 10, 1);

        // when & then
        mockMvc.perform(get("/api/v1/expenditures/statistics")
                        .header("Authorization", accessToken)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("expenditure/get-statistics-invalid-date-range",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer)")
                        ),
                        queryParameters(
                                parameterWithName("startDate").description("조회 시작 날짜 (종료 날짜보다 이후)"),
                                parameterWithName("endDate").description("조회 종료 날짜 (시작 날짜보다 이전)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E422: 유효하지 않은 날짜 범위)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT).optional()
                                        .description("에러 상세 정보 (선택적)"),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING).optional()
                                        .description("에러가 발생한 필드"),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING).optional()
                                        .description("에러 사유")
                        )
                ));
    }
    */
}
