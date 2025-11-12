package com.stdev.smartmealtable.api.onboarding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import com.stdev.smartmealtable.storage.db.budget.DailyBudgetJpaRepository;
import com.stdev.smartmealtable.storage.db.budget.MealBudgetJpaRepository;
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
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 온보딩 - 예산 설정 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SetBudgetControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private DailyBudgetJpaRepository dailyBudgetJpaRepository;

    @Autowired
    private MealBudgetJpaRepository mealBudgetJpaRepository;

    private Long authenticatedMemberId;

    @BeforeEach
    void setUp() {
        // 테스트용 그룹 생성
        Group testGroup = Group.create("서울대학교", GroupType.UNIVERSITY, Address.of("서울대학교", null, "서울특별시 관악구", null, null, null, null));
        Group savedGroup = groupRepository.save(testGroup);

        // 테스트용 회원 생성
        Member testMember = Member.create(savedGroup.getGroupId(), "테스트유저", null, RecommendationType.BALANCED);
        Member savedMember = memberRepository.save(testMember);
        authenticatedMemberId = savedMember.getMemberId();

        // 테스트용 인증 정보 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                savedMember.getMemberId(),
                "test@example.com",
                "hashedPasswordForTest",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);
    }


    @Test
    @DisplayName("예산 설정 성공 - 정상 요청")
    void setBudget_success() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", 300000);
        request.put("dailyBudget", 10000);

        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        mealBudgets.put("LUNCH", 4000);
        mealBudgets.put("DINNER", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.monthlyBudget").value(300000))
                .andExpect(jsonPath("$.data.dailyBudget").value(10000))
                .andExpect(jsonPath("$.data.mealBudgets").isArray())
                // 응답은 오늘의 요청된 식사별 예산만 포함 (API 응답 크기 최소화)
                .andExpect(jsonPath("$.data.mealBudgets").value(hasSize(3)))
                .andExpect(jsonPath("$.data.mealBudgets[*].mealType").isArray())
                .andExpect(jsonPath("$.data.mealBudgets[*].budget").isArray());
    }

    @Test
    @DisplayName("예산 설정 실패 - 월별 예산 null")
    void setBudget_monthlyBudgetNull() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("dailyBudget", 10000);
        
        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("예산 설정 실패 - 일일 예산 null")
    void setBudget_dailyBudgetNull() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", 300000);
        
        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("예산 설정 실패 - 식사별 예산 null")
    void setBudget_mealBudgetsNull() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", 300000);
        request.put("dailyBudget", 10000);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("예산 설정 실패 - 음수 예산")
    void setBudget_negativeBudget() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", -1000);
        request.put("dailyBudget", 10000);
        
        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("예산 설정 실패 - JWT 토큰 없음")
    void setBudget_noToken() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", 300000);
        request.put("dailyBudget", 10000);

        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("예산 설정 성공 - 오늘부터 월말까지의 모든 날짜에 대해 일일 예산과 식사별 예산이 생성된다")
    void setBudget_success_creates_budgets_until_end_of_month() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", 300000);
        request.put("dailyBudget", 10000);

        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        mealBudgets.put("LUNCH", 4000);
        mealBudgets.put("DINNER", 3000);
        request.put("mealBudgets", mealBudgets);

        LocalDate today = LocalDate.now();
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();
        long expectedDayCount = java.time.temporal.ChronoUnit.DAYS.between(today, endOfMonth) + 1;
        long expectedMealBudgetCount = expectedDayCount * 3; // 각 날짜마다 3개의 식사별 예산

        // when
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"));

        // then - 데이터베이스에서 생성된 예산 검증
        long actualDailyBudgetCount = dailyBudgetJpaRepository.countByMemberIdAndBudgetDateBetween(
                authenticatedMemberId, today, endOfMonth);
        long actualMealBudgetCount = mealBudgetJpaRepository.countByBudgetDateBetween(
                today, endOfMonth);

        assertThat(actualDailyBudgetCount)
                .as("오늘부터 월말까지의 일일 예산 개수")
                .isEqualTo(expectedDayCount);

        assertThat(actualMealBudgetCount)
                .as("오늘부터 월말까지의 식사별 예산 개수 (3개씩)")
                .isEqualTo(expectedMealBudgetCount);
    }

    @Test
    @DisplayName("예산 설정 성공 - OTHER 타입을 포함한 경우")
    void setBudget_success_with_other_meal_type() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", 300000);
        request.put("dailyBudget", 10000);

        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        mealBudgets.put("LUNCH", 4000);
        mealBudgets.put("DINNER", 3000);
        mealBudgets.put("OTHER", 2000); // OTHER 타입 추가
        request.put("mealBudgets", mealBudgets);

        LocalDate today = LocalDate.now();
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();
        long expectedDayCount = java.time.temporal.ChronoUnit.DAYS.between(today, endOfMonth) + 1;
        long expectedMealBudgetCount = expectedDayCount * 4; // 각 날짜마다 4개의 식사별 예산

        // when & then - 응답 검증 (오늘의 4가지 식사별 예산만 포함)
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.monthlyBudget").value(300000))
                .andExpect(jsonPath("$.data.dailyBudget").value(10000))
                .andExpect(jsonPath("$.data.mealBudgets").value(hasSize(4))); // 요청한 4가지 식사별 예산이 응답에 포함

        // then - 데이터베이스에서 생성된 예산 검증
        long actualDailyBudgetCount = dailyBudgetJpaRepository.countByMemberIdAndBudgetDateBetween(
                authenticatedMemberId, today, endOfMonth);
        long actualMealBudgetCount = mealBudgetJpaRepository.countByBudgetDateBetween(
                today, endOfMonth);

        assertThat(actualDailyBudgetCount)
                .as("오늘부터 월말까지의 일일 예산 개수")
                .isEqualTo(expectedDayCount);

        assertThat(actualMealBudgetCount)
                .as("오늘부터 월말까지의 식사별 예산 개수 (4개씩)")
                .isEqualTo(expectedMealBudgetCount);
    }

    // === Helper Methods ===

    /**
     * JWT 토큰을 생성하여 "Bearer {token}" 형식으로 반환
     */
    private String createAuthorizationHeader(Long memberId) {
        String token = jwtTokenProvider.createToken(memberId);
        return "Bearer " + token;
    }
}
