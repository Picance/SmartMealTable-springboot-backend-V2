package com.stdev.smartmealtable.api.budget.controller;

import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.config.MockChatModelConfig;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 월별 예산 조회 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({MockChatModelConfig.class})
class MonthlyBudgetQueryControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MonthlyBudgetRepository monthlyBudgetRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Long authenticatedMemberId;
    private String authToken;

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

        // 토큰 생성
        authToken = jwtTokenProvider.createToken(authenticatedMemberId);
    }

    @Test
    @DisplayName("월별 예산 조회 성공 - 예산 정보 반환")
    void getMonthlyBudget_Success() throws Exception {
        // given
        String budgetMonth = "2025-10";
        MonthlyBudget monthlyBudget = MonthlyBudget.create(authenticatedMemberId, 300000, budgetMonth);
        // 150,000원 사용
        monthlyBudget.addUsedAmount(150000);
        monthlyBudgetRepository.save(monthlyBudget);

        // when & then
        mockMvc.perform(get("/api/v1/budgets/monthly")
                        .header("Authorization", "Bearer " + authToken)
                        .param("year", "2025")
                        .param("month", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.year").value(2025))
                .andExpect(jsonPath("$.data.month").value(10))
                .andExpect(jsonPath("$.data.totalBudget").value(300000))
                .andExpect(jsonPath("$.data.totalSpent").value(150000))
                .andExpect(jsonPath("$.data.remainingBudget").value(150000))
                .andExpect(jsonPath("$.data.utilizationRate").value(50.00))
                .andExpect(jsonPath("$.data.daysRemaining").isNumber());
    }

    @Test
    @DisplayName("월별 예산 조회 실패 - 존재하지 않는 예산 (404)")
    void getMonthlyBudget_NotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/budgets/monthly")
                        .header("Authorization", "Bearer " + authToken)
                        .param("year", "2025")
                        .param("month", "11")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("해당 월의 예산 정보를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("월별 예산 조회 실패 - 유효하지 않은 토큰 (401)")
    void getMonthlyBudget_InvalidToken() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/budgets/monthly")
                        .header("Authorization", "Bearer invalid-token")
                        .param("year", "2025")
                        .param("month", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"));
    }

    @Test
    @DisplayName("월별 예산 조회 실패 - 잘못된 월 범위 (400)")
    void getMonthlyBudget_InvalidMonth() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/budgets/monthly")
                        .header("Authorization", "Bearer " + authToken)
                        .param("year", "2025")
                        .param("month", "13")  // 잘못된 월
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())  // 400 Bad Request
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E400"));
    }
}
