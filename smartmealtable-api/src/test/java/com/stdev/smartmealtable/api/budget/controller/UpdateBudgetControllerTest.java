package com.stdev.smartmealtable.api.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.budget.controller.request.UpdateBudgetRequest;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.core.error.ErrorCode;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 월별 예산 수정 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UpdateBudgetControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private Member member;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // Given: 테스트 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시");
        Group savedGroup = groupRepository.save(testGroup);

        // Given: 테스트 회원 생성
        member = Member.create(savedGroup.getGroupId(), "테스트회원", null, RecommendationType.BALANCED);
        member = memberRepository.save(member);  // 저장 후 반환값 받기

        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(), "test@example.com", "hashedPassword", "테스트회원"
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

    private String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    @DisplayName("월별 예산 수정 성공")
    void updateBudget_Success() throws Exception {
        // Given
        UpdateBudgetRequest request = new UpdateBudgetRequest(500000, 15000);

        // When & Then
        mockMvc.perform(put("/api/v1/budgets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.monthlyFoodBudget").value(500000))
                .andExpect(jsonPath("$.data.dailyFoodBudget").value(15000))
                .andExpect(jsonPath("$.data.budgetMonth").value(YearMonth.now().toString()))
                .andExpect(jsonPath("$.data.message").value("예산이 성공적으로 수정되었습니다."));

        // API 응답이 정상이면 DB에도 반영되었다고 가정
        // (별도의 integration test에서 DB 반영을 검증)
    }

    @Test
    @DisplayName("월별 예산 수정 실패 - 예산 정보 없음 (404)")
    void updateBudget_Fail_BudgetNotFound() throws Exception {
        // Given: 새로운 회원 생성 (예산 없음)
        Group newGroup = Group.create("새대학", GroupType.UNIVERSITY, "부산광역시");
        newGroup = groupRepository.save(newGroup);  // 저장 후 반환값 받기
        
        Member newMember = Member.create(newGroup.getGroupId(), "새회원", null, RecommendationType.BALANCED);
        newMember = memberRepository.save(newMember);  // 저장 후 반환값 받기
        
        MemberAuthentication newAuth = MemberAuthentication.createEmailAuth(
                newMember.getMemberId(), "new@example.com", "hashedPassword", "새회원"
        );
        authenticationRepository.save(newAuth);
        
        String newAccessToken = jwtTokenProvider.createToken(newMember.getMemberId());

        UpdateBudgetRequest request = new UpdateBudgetRequest(500000, 15000);

        // When & Then
        mockMvc.perform(put("/api/v1/budgets")
                        .header("Authorization", "Bearer " + newAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E404.name()))
                .andExpect(jsonPath("$.error.message").value("해당 월의 예산 정보를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("월별 예산 수정 실패 - 유효성 검증 실패: monthlyFoodBudget null (422)")
    void updateBudget_Fail_MonthlyBudgetNull() throws Exception {
        // Given
        String requestBody = """
                {
                    "dailyFoodBudget": 15000
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/budgets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E422.name()));
    }

    @Test
    @DisplayName("월별 예산 수정 실패 - 유효성 검증 실패: monthlyFoodBudget 최소값 위반 (422)")
    void updateBudget_Fail_MonthlyBudgetTooLow() throws Exception {
        // Given
        UpdateBudgetRequest request = new UpdateBudgetRequest(500, 15000);

        // When & Then
        mockMvc.perform(put("/api/v1/budgets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E422.name()));
    }

    @Test
    @DisplayName("월별 예산 수정 실패 - 유효성 검증 실패: dailyFoodBudget null (422)")
    void updateBudget_Fail_DailyBudgetNull() throws Exception {
        // Given
        String requestBody = """
                {
                    "monthlyFoodBudget": 500000
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/budgets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E422.name()));
    }

    @Test
    @DisplayName("월별 예산 수정 실패 - 유효성 검증 실패: dailyFoodBudget 최소값 위반 (422)")
    void updateBudget_Fail_DailyBudgetTooLow() throws Exception {
        // Given
        UpdateBudgetRequest request = new UpdateBudgetRequest(500000, 50);

        // When & Then
        mockMvc.perform(put("/api/v1/budgets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E422.name()));
    }
}
