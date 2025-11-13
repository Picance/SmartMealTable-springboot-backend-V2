package com.stdev.smartmealtable.api.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.budget.controller.request.CreateMonthlyBudgetRequest;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.config.MockChatModelConfig;
import com.stdev.smartmealtable.api.config.MockExpenditureServiceConfig;
import com.stdev.smartmealtable.core.error.ErrorCode;
import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
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

import java.time.YearMonth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 월별 예산 등록 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({MockChatModelConfig.class, MockExpenditureServiceConfig.class})
class CreateMonthlyBudgetControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository authenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MonthlyBudgetRepository monthlyBudgetRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Member member;
    private String accessToken;

    @BeforeEach
    void setUp() {
        Group group = Group.create(
                "예산대학교",
                GroupType.UNIVERSITY,
                Address.of("예산대학교", null, "서울특별시", null, null, null, AddressType.HOME)
        );
        Group savedGroup = groupRepository.save(group);

        member = Member.create(savedGroup.getGroupId(), "예산등록회원", null, RecommendationType.BALANCED);
        member = memberRepository.save(member);

        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(),
                "budget-create@example.com",
                "hashedPassword",
                "예산등록회원"
        );
        authenticationRepository.save(auth);

        accessToken = jwtTokenProvider.createToken(member.getMemberId());
    }

    @Test
    @DisplayName("월별 예산 등록 성공")
    void createMonthlyBudget_success() throws Exception {
        // given
        String budgetMonth = YearMonth.now().toString();
        CreateMonthlyBudgetRequest request = new CreateMonthlyBudgetRequest(300000, budgetMonth);

        // when & then
        mockMvc.perform(post("/api/v1/budgets/monthly")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.monthlyFoodBudget").value(300000))
                .andExpect(jsonPath("$.data.budgetMonth").value(budgetMonth))
                .andExpect(jsonPath("$.data.message").value("예산이 성공적으로 등록되었습니다."));
    }

    @Test
    @DisplayName("월별 예산 등록 실패 - 이미 예산이 존재함")
    void createMonthlyBudget_conflict_whenExists() throws Exception {
        // given
        String budgetMonth = YearMonth.now().toString();
        MonthlyBudget existing = MonthlyBudget.create(member.getMemberId(), 250000, budgetMonth);
        monthlyBudgetRepository.save(existing);

        CreateMonthlyBudgetRequest request = new CreateMonthlyBudgetRequest(300000, budgetMonth);

        // when & then
        mockMvc.perform(post("/api/v1/budgets/monthly")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E409.name()))
                .andExpect(jsonPath("$.error.message").value("이미 해당 월의 예산이 등록되어 있습니다."));
    }
}
