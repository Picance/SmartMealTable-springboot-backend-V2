package com.stdev.smartmealtable.api.budget.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 예산 관리 API REST Docs 테스트
 */
@DisplayName("BudgetController REST Docs 테스트")
@Transactional
class BudgetControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository authenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MonthlyBudgetRepository monthlyBudgetRepository;

    @Autowired
    private DailyBudgetRepository dailyBudgetRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Member member;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시");
        Group savedGroup = groupRepository.save(testGroup);

        // 테스트 회원 생성
        member = Member.create(savedGroup.getGroupId(), "예산테스트회원", RecommendationType.BALANCED);
        member = memberRepository.save(member);

        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(), "budget@example.com", "hashedPassword", "예산테스트회원"
        );
        authenticationRepository.save(auth);

        // JWT 토큰 생성
        accessToken = jwtTokenProvider.createToken(member.getMemberId());

        // 현재 월 예산 생성
        String currentMonth = YearMonth.now().toString();
        MonthlyBudget monthlyBudget = MonthlyBudget.create(member.getMemberId(), 300000, currentMonth);
        monthlyBudgetRepository.save(monthlyBudget);

        // 해당 월의 일일 예산 생성 (3개)
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 3; i++) {
            LocalDate date = today.plusDays(i);
            DailyBudget dailyBudget = DailyBudget.create(member.getMemberId(), 10000, date);
            dailyBudgetRepository.save(dailyBudget);
        }
    }

    @Test
    @DisplayName("월별 예산 조회 성공")
    void getMonthlyBudget_success_docs() throws Exception {
        // given
        YearMonth currentMonth = YearMonth.now();
        int year = currentMonth.getYear();
        int month = currentMonth.getMonthValue();

        // when & then
        mockMvc.perform(get("/api/v1/budgets/monthly")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.year").value(year))
                .andExpect(jsonPath("$.data.month").value(month))
                .andDo(document("budget/get-monthly-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        queryParameters(
                                parameterWithName("year")
                                        .description("조회할 연도 (2000-2100)"),
                                parameterWithName("month")
                                        .description("조회할 월 (1-12)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.year")
                                        .type(JsonFieldType.NUMBER)
                                        .description("연도"),
                                fieldWithPath("data.month")
                                        .type(JsonFieldType.NUMBER)
                                        .description("월"),
                                fieldWithPath("data.totalBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("해당 월 전체 예산"),
                                fieldWithPath("data.totalSpent")
                                        .type(JsonFieldType.NUMBER)
                                        .description("해당 월 사용한 금액"),
                                fieldWithPath("data.remainingBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("남은 예산"),
                                fieldWithPath("data.utilizationRate")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예산 사용률 (%)"),
                                fieldWithPath("data.daysRemaining")
                                        .type(JsonFieldType.NUMBER)
                                        .description("남은 일수"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("일별 예산 조회 성공")
    void getDailyBudget_success_docs() throws Exception {
        // given
        LocalDate today = LocalDate.now();

        // when & then
        mockMvc.perform(get("/api/v1/budgets/daily")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("date", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.date").value(today.toString()))
                .andDo(document("budget/get-daily-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        queryParameters(
                                parameterWithName("date")
                                        .description("조회할 날짜 (YYYY-MM-DD 형식)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.date")
                                        .type(JsonFieldType.STRING)
                                        .description("조회한 날짜"),
                                fieldWithPath("data.totalBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("일일 총 예산"),
                                fieldWithPath("data.totalSpent")
                                        .type(JsonFieldType.NUMBER)
                                        .description("사용한 금액"),
                                fieldWithPath("data.remainingBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("남은 예산"),
                                fieldWithPath("data.mealBudgets")
                                        .type(JsonFieldType.ARRAY)
                                        .optional()
                                        .description("끼니별 예산 목록"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("월별 예산 수정 성공")
    void updateBudget_success_docs() throws Exception {
        // given
        String requestBody = """
                {
                    "monthlyFoodBudget": 400000,
                    "dailyFoodBudget": 15000
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/budgets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.monthlyFoodBudget").value(400000))
                .andExpect(jsonPath("$.data.dailyFoodBudget").value(15000))
                .andDo(document("budget/update-monthly-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        requestFields(
                                fieldWithPath("monthlyFoodBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("월별 식비 예산 (0 이상)"),
                                fieldWithPath("dailyFoodBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("일일 식비 예산 (0 이상)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.monthlyBudgetId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("월별 예산 ID"),
                                fieldWithPath("data.monthlyFoodBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("수정된 월별 예산"),
                                fieldWithPath("data.dailyFoodBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("수정된 일일 예산"),
                                fieldWithPath("data.budgetMonth")
                                        .type(JsonFieldType.STRING)
                                        .description("예산 적용 월 (YYYY-MM)"),
                                fieldWithPath("data.message")
                                        .type(JsonFieldType.STRING)
                                        .description("처리 결과 메시지"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("월별 예산 수정 실패 - 유효성 검증 실패")
    void updateBudget_validation_docs() throws Exception {
        // given
        String requestBody = """
                {
                    "monthlyFoodBudget": -1000,
                    "dailyFoodBudget": -500
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/budgets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andDo(document("budget/update-monthly-validation",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        requestFields(
                                fieldWithPath("monthlyFoodBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("월별 식비 예산 (음수 - 유효하지 않음)"),
                                fieldWithPath("dailyFoodBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("일일 식비 예산 (음수 - 유효하지 않음)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("응답 데이터 (에러 시 null)"),
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
                                        .description("에러 상세 데이터"),
                                fieldWithPath("error.data.field")
                                        .type(JsonFieldType.STRING)
                                        .description("검증 실패한 필드명"),
                                fieldWithPath("error.data.reason")
                                        .type(JsonFieldType.STRING)
                                        .description("검증 실패 이유")
                        )
                ));
    }

    @Test
    @DisplayName("월별 예산 조회 실패 - 잘못된 파라미터")
    void getMonthlyBudget_invalidParams_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/budgets/monthly")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("year", "1999")  // 2000 미만
                        .param("month", "13"))  // 12 초과
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E400"))
                .andDo(document("budget/get-monthly-invalid-params",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        queryParameters(
                                parameterWithName("year")
                                        .description("조회할 연도 (유효하지 않음)"),
                                parameterWithName("month")
                                        .description("조회할 월 (유효하지 않음)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("응답 데이터 (에러 시 null)"),
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
                                        .optional()
                                        .description("에러 상세 데이터 (파라미터 검증 실패 시 null 가능)")
                        )
                ));
    }

    @Test
    @DisplayName("일별 예산 수정 성공")
    void updateDailyBudget_success_docs() throws Exception {
        // given
        LocalDate today = LocalDate.now();
        String requestBody = """
                {
                    "dailyFoodBudget": 15000,
                    "applyForward": false
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/budgets/daily/{date}", today)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.dailyFoodBudget").value(15000))
                .andDo(document("budget/update-daily-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer {accessToken}")
                        ),
                        pathParameters(
                                parameterWithName("date")
                                        .description("수정할 날짜 (YYYY-MM-DD 형식)")
                        ),
                        requestFields(
                                fieldWithPath("dailyFoodBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("일일 식비 예산 (0 이상)"),
                                fieldWithPath("applyForward")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("이후 날짜 적용 여부 (true: 해당 날짜 이후 모두 수정, false: 해당 날짜만)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.budgetId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("일별 예산 ID"),
                                fieldWithPath("data.dailyFoodBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("수정된 일일 예산"),
                                fieldWithPath("data.budgetDate")
                                        .type(JsonFieldType.STRING)
                                        .description("예산 적용 날짜"),
                                fieldWithPath("data.appliedForward")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("이후 날짜 적용 여부"),
                                fieldWithPath("data.updatedCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("수정된 예산 개수"),
                                fieldWithPath("data.message")
                                        .type(JsonFieldType.STRING)
                                        .description("처리 결과 메시지"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("월별 예산 조회 실패 - 인증되지 않은 요청")
    void getMonthlyBudget_unauthorized_docs() throws Exception {
        // given
        YearMonth currentMonth = YearMonth.now();
        int year = currentMonth.getYear();
        int month = currentMonth.getMonthValue();

        // when & then
        mockMvc.perform(get("/api/v1/budgets/monthly")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").exists())
                .andDo(document("budget/get-monthly-unauthorized",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("year")
                                        .description("조회할 연도 (2000-2100)"),
                                parameterWithName("month")
                                        .description("조회할 월 (1-12)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("응답 데이터 (에러 시 null)"),
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
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 상세 데이터 (401 에러 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("월별 예산 수정 실패 - 인증되지 않은 요청")
    void updateBudget_unauthorized_docs() throws Exception {
        // given
        String requestBody = """
                {
                    "monthlyFoodBudget": 400000,
                    "dailyFoodBudget": 15000
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").exists())
                .andDo(document("budget/update-monthly-unauthorized",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("monthlyFoodBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("월별 식비 예산 (0 이상)"),
                                fieldWithPath("dailyFoodBudget")
                                        .type(JsonFieldType.NUMBER)
                                        .description("일일 식비 예산 (0 이상)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("응답 데이터 (에러 시 null)"),
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
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 상세 데이터 (401 에러 시 null)")
                        )
                ));
    }
}

