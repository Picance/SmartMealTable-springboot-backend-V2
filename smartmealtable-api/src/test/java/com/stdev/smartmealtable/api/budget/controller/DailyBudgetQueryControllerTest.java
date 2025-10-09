package com.stdev.smartmealtable.api.budget.controller;

import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.budget.MealBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MealType;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 일별 예산 조회 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DailyBudgetQueryControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DailyBudgetRepository dailyBudgetRepository;

    @Autowired
    private MealBudgetRepository mealBudgetRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Long authenticatedMemberId;
    private String authToken;

    @BeforeEach
    void setUp() {
        // 테스트용 그룹 생성
        Group testGroup = Group.create("서울대학교", GroupType.UNIVERSITY, "서울특별시 관악구");
        Group savedGroup = groupRepository.save(testGroup);

        // 테스트용 회원 생성
        Member testMember = Member.create(savedGroup.getGroupId(), "테스트유저", RecommendationType.BALANCED);
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

        // 토큰 생성
        authToken = jwtTokenProvider.createToken(authenticatedMemberId);
    }

    @Test
    @DisplayName("일별 예산 조회 성공 - 끼니별 예산 포함")
    void getDailyBudget_Success() throws Exception {
        // given
        LocalDate targetDate = LocalDate.of(2025, 10, 8);
        
        DailyBudget dailyBudget = DailyBudget.create(authenticatedMemberId, 10000, targetDate);
        dailyBudget.addUsedAmount(5000);
        DailyBudget savedDailyBudget = dailyBudgetRepository.save(dailyBudget);

        // 끼니별 예산 생성
        MealBudget breakfast = MealBudget.create(savedDailyBudget.getBudgetId(), 3000, MealType.BREAKFAST, targetDate);
        breakfast.addUsedAmount(2000);
        mealBudgetRepository.save(breakfast);

        MealBudget lunch = MealBudget.create(savedDailyBudget.getBudgetId(), 4000, MealType.LUNCH, targetDate);
        lunch.addUsedAmount(3000);
        mealBudgetRepository.save(lunch);

        MealBudget dinner = MealBudget.create(savedDailyBudget.getBudgetId(), 3000, MealType.DINNER, targetDate);
        mealBudgetRepository.save(dinner);

        // when & then
        mockMvc.perform(get("/api/v1/budgets/daily")
                        .header("Authorization", "Bearer " + authToken)
                        .param("date", "2025-10-08")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.date").value("2025-10-08"))
                .andExpect(jsonPath("$.data.totalBudget").value(10000))
                .andExpect(jsonPath("$.data.totalSpent").value(5000))
                .andExpect(jsonPath("$.data.remainingBudget").value(5000))
                .andExpect(jsonPath("$.data.mealBudgets", hasSize(3)))
                .andExpect(jsonPath("$.data.mealBudgets[0].mealType").value("BREAKFAST"))
                .andExpect(jsonPath("$.data.mealBudgets[0].budget").value(3000))
                .andExpect(jsonPath("$.data.mealBudgets[0].spent").value(2000))
                .andExpect(jsonPath("$.data.mealBudgets[0].remaining").value(1000));
    }

    @Test
    @DisplayName("일별 예산 조회 실패 - 존재하지 않는 예산 (404)")
    void getDailyBudget_NotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/budgets/daily")
                        .header("Authorization", "Bearer " + authToken)
                        .param("date", "2025-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("해당 날짜의 예산 정보를 찾을 수 없습니다."));
    }
}
